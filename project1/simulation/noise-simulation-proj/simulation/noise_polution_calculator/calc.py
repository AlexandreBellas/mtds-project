import h3pandas as h3
import h3
import geopandas as geopd

from pathlib import Path
from copy import copy
import time

def save_plot(dataframe):
    temp_geodataframe = geopd.GeoDataFrame(dataframe, crs=3763)
    temp_geodataframe = temp_geodataframe.set_geometry('geometry')
    plot = temp_geodataframe.plot(color='white', edgecolor='black', markersize=0.5, figsize=(15, 15))
    fig = plot.get_figure()
    Path("output").mkdir(parents=True, exist_ok=True)
    fig.savefig("output/bufferedEntities.png")

def noise_per_hexagonal_grid(geo_dataframe_object):
    start_time = time.time()
    geo_dataframe = copy(geo_dataframe_object)
    geo_dataframe['noise_pollution_area'] = geo_dataframe['geometry'].apply(lambda x: x.buffer(50, cap_style=1))
    # geo_dataframe = geo_dataframe.set_geometry('noise_pollution_area')
    geo_dataframe['geometry'] = geo_dataframe['noise_pollution_area']
    geo_dataframe = geo_dataframe.to_crs(4326)
    # df = geo_dataframe.h3.geo_to_h3(15)
    #print(geo_dataframe)
    geo_dataframe_h3 = geo_dataframe.h3.polyfill(14, explode=True)
    end_time = time.time()
    time_elapsed = (end_time - start_time)

    count_series = geo_dataframe_h3.groupby('h3_polyfill').size()
    new_df = count_series.to_frame(name='amount').reset_index()
    new_df = new_df.sort_values(by=['amount'])
    save_plot(geo_dataframe)
    print(new_df)
    print(geo_dataframe_h3)
    print("TIME noise_per_hexagonal_grid: " + str(time_elapsed))
