import numpy as np
import random
import pandas as pd
import geopandas as geopd
import movingpandas as mpd
from shapely.geometry import Point, LineString, Polygon, box, mapping
from datetime import datetime
from pyproj import crs
from functools import reduce, partial
from dotenv import dotenv_values

config = dotenv_values(".env")

## span entities within the polygon of the region
#spawned_point = Point(random.uniform(polygon.bounds.minx.values[0], polygon.bounds.maxx.values[0]), random.uniform(polygon.bounds.miny.values[0], polygon.bounds.maxy.values[0]))

x_coord_adding = random.randrange(-100, 100)
y_coord_adding = random.randrange(-100, 100)
def add_needed_col_to_vehicle(df):
    df['type']='vehicle'
    df['history']=[]
    df['movement_direction']={'add_to_x':random.randrange(-config.moving_speed_vehicles, config.moving_speed_vehicles),'add_to_y': random.randrange(-config.moving_speed_vehicles,config.moving_speed_vehicles)}
    df['movement_area']=df['geometry'].buffer(config.area_of_movemenet, cap_style=3)
    df['movement_speed']= config.moving_speed_vehicles    
    df['noise_volume']= config.noise_level_vehicles
    df['noise_pollution_area']= config.affected_area_by_vehicles
    return df

def add_needed_col_to_people(df):
    df['type']='person'
    df['history']=[]
    df['movement_direction']={'add_to_x':random.randrange(-config.moving_speed_people, config.moving_speed_people),'add_to_y': random.randrange(-config.moving_speed_people,config.moving_speed_people)}
    df['movement_area']=df['geometry'].buffer(config.rea_of_movemenet, cap_style=3)
    df['movement_speed']= config.moving_speed_people    
    df['noise_volume']= config.noise_level_people
    df['noise_pollution_area']= config.affected_area_by_people
    return df

def (region_object_row):
    counter_people = 0
    counter_vehicle = 0

    spawned_people = []
    spawned_vehicles = []
    #print(region_object_row['NAME_3'])
    #print(type(region_object_row))
    polygon = region_object_row['geometry']

    while len(spawned_people) < config.number_of_people: 
        spawned_point = Point(random.uniform(polygon.bounds.minx.values[0], polygon.bounds.maxx.values[0]), random.uniform(polygon.bounds.miny.values[0], polygon.bounds.maxy.values[0]))
        if(region_object_row['geometry'].contains(spawned_point).values[0]):
            spawned_people.append(spawned_point)
        counter_people += 1  

    while len(spawned_vehicles) < config.number_of_vehicles: 
        spawned_point = Point(random.uniform(polygon.bounds.minx.values[0], polygon.bounds.maxx.values[0]), random.uniform(polygon.bounds.miny.values[0], polygon.bounds.maxy.values[0]))
        if(region_object_row['geometry'].contains(spawned_point).values[0]):
            spawned_vehicles.append(spawned_point)
        counter_vehicle += 1
        
    #transforming data to Dataframe and adding columns for PEOPLE
    spawned_people_df = pd.DataFrame(spawned_people, columns = ['geometry'])
    spawned_people_geodf = geopd.GeoDataFrame(spawned_people_df, crs=3763)
    spawned_people_geodf = spawned_people_geodf.apply(add_needed_col_to_people, axis=1)

    #transforming data to Dataframe and adding columns for VEHICLES
    spawned_vehicles_df = pd.DataFrame(spawned_vehicles, columns = ['geometry'])
    spawned_vehicles_geodf = geopd.GeoDataFrame(spawned_vehicles_df, crs=3763)
    spawned_vehicles_geodf = spawned_vehicles_geodf.apply(add_needed_col_to_vehicle, axis=1)

    frames = [spawned_vehicles_geodf, spawned_people_geodf]
    result = pd.concat(frames, ignore_index=True)
    print(region_object_row['NAME_3'].values[0])
    result['region_affiliation'] =region_object_row['NAME_3'].values[0]
    return result