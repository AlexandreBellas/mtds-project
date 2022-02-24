import sys
from initialization.load_regions import load_country
from initialization.spawn_entities import spawn_entities
from simulation_engine.simulateMovement import start_simulation

from dotenv import dotenv_values
import geopandas as geopd

config = dotenv_values(".env")


def main(args=None):
    """The main routine."""
    if args is None:
        args = sys.argv[1:]

    gdf = load_country()

    print(int(config.get("NUMBER_OF_PEOPLE")))
    gdf_oldenburg = gdf[gdf['NAME_3'] == 'Schweinfurt Städte']

    spawned_entities_df = spawn_entities(gdf_oldenburg)

    while True:
        spawned_entities_df = start_simulation(spawned_entities_df)
        print(len(spawned_entities_df['history'][0]))

    # Do argument parsing here (eg. with argparse) and anything else
    # you want your project to do. Return values are exit codes.


if __name__ == "__main__":
    # app.run()
    sys.exit(main())
