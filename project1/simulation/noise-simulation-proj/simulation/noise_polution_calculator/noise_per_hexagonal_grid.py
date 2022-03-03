from pathlib import Path
from copy import copy
import time
from .decibel_calculator import decibel_calculator


def save_plot(dataframe):
    plot = dataframe.plot(color='white', edgecolor='black', markersize=0.5, figsize=(15, 15))
    fig = plot.get_figure()
    Path("output").mkdir(parents=True, exist_ok=True)
    fig.savefig("output/bufferedEntities.png")


def noise_per_hexagonal_grid(geo_dataframe_object):
    start_time_total = time.time()
    geo_dataframe = copy(geo_dataframe_object)

    geo_dataframe['geometry'] = geo_dataframe.apply(
        lambda x: x['geometry'].buffer(x.noise_pollution_area, cap_style=1), axis=1)
    geo_dataframe = geo_dataframe.to_crs(4326)

    end_time = time.time()
    time_elapsed = (end_time - start_time_total)
    print(geo_dataframe.shape)
    print(geo_dataframe.head(10))
    print("TIME 0_noise_per_hexagonal_grid: " + str(time_elapsed))
    geo_dataframe.to_csv("./data.csv")
    start_time = time.time()
    geo_dataframe_h3 = geo_dataframe.h3.polyfill(14, explode=True)
    end_time = time.time()
    time_elapsed = (end_time - start_time)
    print("TIME 1_noise_per_hexagonal_grid: " + str(time_elapsed))

    start_time = time.time()
    geo_dataframe_h3['counted'] = geo_dataframe_h3['h3_polyfill'].apply(lambda x: len(x))
    end_time = time.time()
    time_elapsed = (end_time - start_time)
    print("TIME 1_noise_per_hexagonal_grid: " + str(time_elapsed))
    print(geo_dataframe_h3.head(20))

    start_time = time.time()
    count_series = geo_dataframe_h3.groupby(by=['h3_polyfill'])['noise_volume'].apply(list).reset_index(
        name="noise_volume_list")
    end_time = time.time()
    time_elapsed = (end_time - start_time)
    print("TIME 2_noise_per_hexagonal_grid: " + str(time_elapsed))

    count_series['aggregated_noise_level'] = count_series.apply(lambda x: decibel_calculator(x.noise_volume_list),
                                                                axis=1)
    # count_series = count_series.to_frame().reset_index()
    count_series = count_series.sort_values(by=['noise_volume_list'])
    save_plot(geo_dataframe)
    # print(count_series)
    # print(geo_dataframe_h3)
    end_time_total = time.time()
    time_elapsed = (end_time_total - start_time_total)
    print("TIME TOTALnoise_per_hexagonal_grid: " + str(time_elapsed))

    return count_series

