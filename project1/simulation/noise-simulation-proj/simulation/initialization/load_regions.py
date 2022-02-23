import geopandas as geopd


# loading geojson of the german map and its regions
def load_country():
    fname = "resources/2_hoch.geojson.json"
    file = open(fname)
    df = geopd.read_file(file)
    gdf = geopd.GeoDataFrame(df)
    gdf = gdf.to_crs(epsg=3763)

    return gdf
