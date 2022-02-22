import numpy as np
import googlemaps
import random
import pandas as pd
import geopandas as geopd
import movingpandas as mpd
from shapely.geometry import Point, LineString, Polygon, box, mapping
from datetime import datetime
from pyproj import CRS
from datetime import datetime
import math
from functools import reduce, partial

## loading geojson of the german map and its regions
import os
def loadCountry():
    fname = "2_hoch.geojson.json"
    file = open(fname)
    df = geopd.read_file(file)
    gdf = geopd.GeoDataFrame(df)
    gdf = gdf.to_crs(epsg=3763)
    
    return gdf

#polygon_bounding_box = Polygon([(1290773.81,1421304.01), (1292065.26,1421613.65), (1292294.97,1420691.03), (1290811.45,1420198.23)])