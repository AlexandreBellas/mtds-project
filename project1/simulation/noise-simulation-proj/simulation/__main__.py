import sys
from initialization.load_regions import load_country
from initialization.spawn_entities import spawn_entities
from simulation_engine.simulateMovement import movement_simulation
from noise_polution_calculator.noise_per_hexagonal_grid import noise_per_hexagonal_grid
from dotenv import dotenv_values
import time
from send_http import mqtt_publish


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
    while True:
        start_time = time.time()
        print("SIMULATION_INTERATION: " + str(interation_counter))
        spawned_entities_df = movement_simulation(spawned_entities_df)
        print(len(spawned_entities_df['history'][0]))
        noise_level_df = noise_per_hexagonal_grid(spawned_entities_df)
        interation_counter = interation_counter + 1
        end_time = time.time()
        time_elapsed = (end_time - start_time)
        print("TIME total iteration: " + str(time_elapsed))
        mqtt_publish(noise_level_df)

    # Do argument parsing here (eg. with argparse) and anything else
    # you want your project to do. Return values are exit codes.


if __name__ == "__main__":
    # app.run()
    sys.exit(main())
