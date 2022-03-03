import sys
from initialization.load_regions import load_country
from initialization.spawn_entities import spawn_entities
from simulation_engine.simulateMovement import movement_simulation
from noise_polution_calculator.pyspark_noise_per_hexagonal_grid import pyspark_noise_per_hexagonal_grid
from dotenv import dotenv_values
import time
from send_http import mqtt_publish
import threading
from queue import LifoQueue

config = dotenv_values(".env")


def main(args=None):
    """The main routine."""
    if args is None:
        args = sys.argv[1:]

    print("INITIALIZATION")
    gdf = load_country()
    print(int(config.get("NUMBER_OF_PEOPLE")))
    gdf_selected_region = gdf[gdf['NAME_3'] == 'Schweinfurt Städte']

    spawned_entities_df = spawn_entities(gdf_selected_region)
    print("SIMULATION_BEGIN")
    interation_counter = 0

    entities_queue = LifoQueue()

    def write_thread(queue: LifoQueue):
        print("write_thread Initialized")
        current_movement_simulation = queue.get()
        print(current_movement_simulation.head(10))
        while True:
            time.sleep(int(config.get("TIME_STEP")))
            print(current_movement_simulation.head(10))
            print("write_thread Calculating")
            noise_level_df = pyspark_noise_per_hexagonal_grid(current_movement_simulation)
            print("write_thread publihing")
            mqtt_publish(noise_level_df.toPandas())
            print("write_thread DONE")

    t1 = threading.Thread(target=write_thread, args=(entities_queue,))
    t1.start()

    while True:
        start_time = time.time()
        print("SIMULATION_INTERATION: " + str(interation_counter))
        entities_queue.put(movement_simulation(spawned_entities_df))
        interation_counter = interation_counter + 1
        end_time = time.time()
        time_elapsed = (end_time - start_time)
        print("TIME total iteration: " + str(time_elapsed))
        time.sleep(1)



if __name__ == "__main__":
    # app.run()
    sys.exit(main())
