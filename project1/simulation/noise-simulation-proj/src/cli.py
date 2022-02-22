from initialization.load_regions import loadCountry 
from initialization.spawn_entities import spawn_people_and_vehicles 

### Spawning moving entities into regions that are defined in geojson -->for now just into the city Oldenburg

## preparing th    test2 = pd.DataFrame([i*2, i*3, i*4])
#test = pd.concat([test, test2], ignore_index=True)e base map of the spawned_vehicles, columns = ['geometry'])region that is currently utilized

gdf = gdf.to_crs(epsg=3763)
#brandenburgbase = brandenburgdf.plot(color='white', edgecolor='black', cmap='Reds', figsize=(10, 10))
polygon_bounding_box = Polygon([(1290773.81,1421304.01), (1292065.26,1421613.65), (1292294.97,1420691.03), (1290811.45,1420198.23)])
newDF = pd.DataFrame([], columns = ['geometry', 'type', 'region_affiliation','history','movement_direction','movement_speed','noise_volume','noise_pollution_area'])
#for index, row in gdf.iterrows():
gdf_oldenburg = gdf[gdf['NAME_3'] == 'Oldenburg']
#new_geodf = geopd.GeoDataFrame(data=[gdf_oldenburg], columns= ['ID_0', 'ISO', 'NAME_0', 'ID_1', 'NAME_1', 'ID_2', 'NAME_2', 'ID_3', 'NAME_3', 'NL_NAME_3', 'VARNAME_3', 'TYPE_3', 'ENGTYPE_3', 'geometry'], crs=3763)
#newgeo = geopd.GeoSeries(data=row,crs=3763)
gdf_oldenburg['geometry'] = polygon_bounding_box
test2 = spawnpeople(gdf_oldenburg)
#test = pd.Series(test2, index = ['geometry', 'type', 'region_affiliation','history','movement_direction','movement_area','movement_speed','noise_volume','noise_pollution_area'])
newDF = newDF.append(test2, ignore_index=True)
gdf_oldenburg
    
    


def main():
    #Step 1. Initialization
    loadCountry()
    result = spawn_people_and_vehicles()

    #Step 2. Initialization
