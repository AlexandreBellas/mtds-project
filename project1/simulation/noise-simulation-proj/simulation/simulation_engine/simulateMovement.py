import time

from geopandas import GeoDataFrame
from pyproj import CRS
from shapely.geometry import Point
import random
import geopandas as geopd
from datetime import datetime


def simulate_movement(array):
    test = array['history'].copy()
    test.append({'geometry': array['geometry'], 't': datetime.now()})
    array['history'] = test
    new_point = Point(array['geometry'].x + array['movement_direction']['add_to_x'],
                      array['geometry'].y + array['movement_direction']['add_to_y'])
    while not array['movement_area'].contains(new_point):
        new_x_coord_adding = random.randrange(-100, 100)
        new_y_coord_adding = random.randrange(-100, 100)
        new_point = Point(array['geometry'].x + new_x_coord_adding, array['geometry'].y + new_y_coord_adding)
        array['movement_direction'] = {'add_to_x': new_x_coord_adding, 'add_to_y': new_y_coord_adding}

    array['geometry'] = new_point
    return array


# test = spawned_people_geodf.apply(simulateMovement, axis=1)
# spawned_people_geodf


def start_simulation(entities_df) -> GeoDataFrame:
    # simulation_engine of people moving in a polygon
    # while len(entities_df['history'][0]) < 2:
    entities_df = entities_df.apply(simulate_movement, axis=1)
    # df = pd.DataFrame(spawned_people_geodf['history'][0]).set_index('t')
    gdf = geopd.GeoDataFrame(entities_df, crs=CRS(3763))

    # frame = spawned_people_geodf.plot(color='green',markersize=3, figsize=(10, 10))
    # agentsdf = gdf.to_crs(epsg=4326)
    # traj = mpd.Trajectory(agentsdf, 1)
    # test = traj.hvplot(geo=True, tiles='OSM', line_width=1, frame_width=500, frame_height=500)
    # display.clear_output(wait=True)
    # display.display(pl.gcf())
    time.sleep(1)
    return gdf
