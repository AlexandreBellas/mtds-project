import json
from pathlib import Path
from copy import copy
from .decibel_calculator import decibel_calculator
import shapely
from pyspark.sql.functions import col
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode
from pyspark.sql import functions as F
from h3_pyspark.indexing import index_shape
from pyspark.sql.functions import udf
from pyspark.sql.types import StringType
from shapely.geometry import Point


def save_plot(dataframe):
    # temp_geodataframe = geopd.GeoDataFrame(dataframe, crs=4326)
    # temp_geodataframe = temp_geodataframe.set_geometry('geometry')
    plot = dataframe.plot(color='white', edgecolor='black', markersize=0.5, figsize=(15, 15))
    fig = plot.get_figure()
    Path("output").mkdir(parents=True, exist_ok=True)
    fig.savefig("output/bufferedEntities.png")


def pyspark_noise_per_hexagonal_grid(geo_dataframe_object):
    spark = SparkSession.builder \
        .appName("Spark Standalone Cluster") \
        .master("spark://172.20.10.3:7077") \
        .config("spark.executor.memory", "4g") \
        .config("spark.executor.cores", "4") \
        .getOrCreate()

    geo_dataframe = copy(geo_dataframe_object)

    geo_dataframe['geometry'] = geo_dataframe.apply(
        lambda x: x['geometry'].buffer(x.noise_pollution_area, cap_style=1), axis=1)

    geo_dataframe['resolution'] = 15
    geo_dataframe = geo_dataframe.to_crs(4326)

    newDF = geo_dataframe.drop("movement_area", axis=1)
    newDF = newDF.drop("history", axis=1)
    newDF['geojson'] = newDF.apply(lambda x: json.dumps(shapely.geometry.mapping(x['geometry']), indent=1), axis=1)
    newDF = newDF.drop("geometry", axis=1)

    df = spark.createDataFrame(newDF)
    df = df.withColumn('h3_15', index_shape('geojson', 'resolution'))
    new = df.withColumn("explode", explode(df.h3_15))
    new = new.groupBy("explode").agg(F.collect_list('noise_volume').alias("noise_volume_list"))
    convertUDF = udf(lambda z: decibel_calculator(z), StringType())
    new = new.withColumn("aggregated_noise_level", convertUDF(col("noise_volume_list")))
    # new.orderBy(desc("aggregated_noise_level")).show()

    return new
