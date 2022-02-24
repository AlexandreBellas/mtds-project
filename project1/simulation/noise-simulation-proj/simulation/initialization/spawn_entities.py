import random
import pandas as pd
import geopandas as geopd
from shapely.geometry import Point
from dotenv import dotenv_values
from pathlib import Path

config = dotenv_values(".env")


def save_plot(dataframe):
    temp_geodataframe = geopd.GeoDataFrame(dataframe, crs=3763)
    temp_geodataframe = temp_geodataframe.set_geometry('geometry')
    plot = temp_geodataframe.plot(color='white', edgecolor='black', markersize=0.5, figsize=(15, 15))
    fig = plot.get_figure()
    Path("output").mkdir(parents=True, exist_ok=True)
    fig.savefig("output/spawned_entities_in_region.png")


def add_needed_col_to_vehicle(df):
    df['type'] = 'vehicle'
    df['history'] = []
    df['movement_direction'] = {
        'add_to_x': random.randrange(-int(config.get("MOVING_SPEED_VEHICLE")), int(config.get("MOVING_SPEED_VEHICLE"))),
        'add_to_y': random.randrange(-int(config.get("MOVING_SPEED_VEHICLE")), int(config.get("MOVING_SPEED_VEHICLE")))}
    # df['movement_area'] = df['geometry'].buffer(int(config.get("AREA_OF_MOVEMENT")), cap_style=3)
    df['movement_speed'] = int(config.get("MOVING_SPEED_VEHICLE"))
    df['noise_volume'] = int(config.get("NOISE_LEVEL_VEHICLES"))
    df['noise_pollution_area'] = int(config.get("AFFECTED_AREA_BY_VEHICLE"))
    return df


def add_needed_col_to_people(df):
    df['type'] = 'person'
    df['history'] = []
    df['movement_direction'] = {
        'add_to_x': random.randrange(-int(config.get("MOVING_SPEED_PEOPLE")), int(config.get("MOVING_SPEED_PEOPLE"))),
        'add_to_y': random.randrange(-int(config.get("MOVING_SPEED_PEOPLE")), int(config.get("MOVING_SPEED_PEOPLE")))}
    df['movement_area'] = df['geometry'].buffer(int(config.get("AREA_OF_MOVEMENT")), cap_style=3)
    df['movement_speed'] = int(config.get("MOVING_SPEED_PEOPLE"))
    df['noise_volume'] = int(config.get("NOISE_LEVEL_PEOPLE"))
    df['noise_pollution_area'] = int(config.get("AFFECTED_AREA_BY_PEOPLE"))
    return df


def spawn_entities(region_object_row):
    counter_people = 0
    counter_vehicle = 0

    spawned_people = []
    spawned_vehicles = []
    # print(region_object_row['NAME_3'])
    # print(type(region_object_row))
    polygon = region_object_row['geometry']

    while len(spawned_people) < int(config.get("NUMBER_OF_PEOPLE")):
        spawned_point = Point(random.uniform(polygon.bounds.minx.values[0], polygon.bounds.maxx.values[0]),
                              random.uniform(polygon.bounds.miny.values[0], polygon.bounds.maxy.values[0]))
        if region_object_row['geometry'].contains(spawned_point).values[0]:
            spawned_people.append(spawned_point)
        counter_people += 1

    while len(spawned_vehicles) < int(config.get("NUMBER_OF_VEHICLES")):
        spawned_point = Point(random.uniform(polygon.bounds.minx.values[0], polygon.bounds.maxx.values[0]),
                              random.uniform(polygon.bounds.miny.values[0], polygon.bounds.maxy.values[0]))
        if region_object_row['geometry'].contains(spawned_point).values[0]:
            spawned_vehicles.append(spawned_point)
        counter_vehicle += 1

    # transforming data to Dataframe and adding columns for PEOPLE
    spawned_people_df = pd.DataFrame(spawned_people, columns=['geometry'])
    spawned_people_geodf = geopd.GeoDataFrame(spawned_people_df, crs=3763)
    spawned_people_geodf = spawned_people_geodf.apply(add_needed_col_to_people, axis=1)

    # transforming data to Dataframe and adding columns for VEHICLES
    spawned_vehicles_df = pd.DataFrame(spawned_vehicles, columns=['geometry'])
    spawned_vehicles_geodf = geopd.GeoDataFrame(spawned_vehicles_df, crs=3763)
    spawned_vehicles_geodf = spawned_vehicles_geodf.apply(add_needed_col_to_vehicle, axis=1)

    frames = [spawned_vehicles_geodf, spawned_people_geodf]
    result = pd.concat(frames, ignore_index=True)
    result['region_affiliation'] = region_object_row['NAME_3'].values[0]
    result['movement_area'] = region_object_row.iloc[0]['geometry']

    dataframe = pd.DataFrame(result, columns=['geometry', 'type', 'region_affiliation', 'history', 'movement_direction',
                                              'movement_speed', 'noise_volume', 'noise_pollution_area',
                                              'movement_area'])
    save_plot(dataframe)

    return dataframe
