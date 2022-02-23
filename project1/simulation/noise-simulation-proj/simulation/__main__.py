import sys
from initialization.load_regions import load_country
from initialization.spawn_entities import spawn_people
import h3
import pandas as pd
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
    newDF = pd.DataFrame([], columns=['geometry', 'type', 'region_affiliation', 'history', 'movement_direction',
                                      'movement_speed', 'noise_volume', 'noise_pollution_area'])
    test2 = spawn_people(gdf_oldenburg)
    print(test2['movement_area'])
    # newDF = newDF.append(test2, ignore_index=True)
    #
    # ## here you can see the potitions of entities at moment x spawned in oldenburg
    # geonewdf = geopd.GeoDataFrame(newDF, crs=3763)
    # geonewdf = geonewdf.set_geometry('geometry')
    # # fix = geonewdf.plot(color='white', edgecolor='black', markersize=0.5, figsize=(15, 15))
    # geonewdf.hvplot(line_width=0.1, frame_width=800, frame_height=800)

    print("This is the main routine.")
    print("It should do something interesting.")

    # Do argument parsing here (eg. with argparse) and anything else
    # you want your project to do. Return values are exit codes.


if __name__ == "__main__":
    # app.run()
    sys.exit(main())
