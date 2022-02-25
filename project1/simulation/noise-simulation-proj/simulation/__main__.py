import sys
from initialization.load_regions import load_country
from initialization.spawn_entities import spawn_entities
from simulation_engine.simulateMovement import movement_simulation
from noise_polution_calculator.noise_per_hexagonal_grid import noise_per_hexagonal_grid
from dotenv import dotenv_values
import requests
import time
import json

config = dotenv_values(".env")


def post_to_node_red(dataframe):
    result = dataframe.to_json(orient="records")
    # parsed = json.loads(result)
    # json.dumps(parsed, indent=4)
    import json
    with open('data.json', 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=4)


    # api-endpoint
    url = "http://localhost:1880/getname"

    # defining a params dict for the parameters to be sent to the API
    data = {'dataframe': result}

    # sending get request and saving the response as response object
    r = requests.post(url=url, data=data)

    # extracting data in json format
    data = r.json()
    print(data)


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
        post_to_node_red(noise_level_df)
    # Do argument parsing here (eg. with argparse) and anything else
    # you want your project to do. Return values are exit codes.


if __name__ == "__main__":
    # app.run()
    sys.exit(main())
