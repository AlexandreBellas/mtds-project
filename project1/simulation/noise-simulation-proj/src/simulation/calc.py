import h3pandas as h3
import h3


def dasd():
    oldenburdf = spawned_people_geodf[spawned_people_geodf['region_affiliation'] == 'Oldenburg']
    geo_oldenburg = geopd.GeoDataFrame(oldenburdf, crs=3763)
    geo_oldenburg['noise_pollution_area']= oldenburdf['geometry'].apply(lambda x: x.buffer(10, cap_style=1))
    #geo_oldenburg = geo_oldenburg.set_geometry('noise_pollution_area')
    geo_oldenburg['geometry'] = geo_oldenburg['noise_pollution_area']
    geo_oldenburg = geo_oldenburg.to_crs(4326)
    #df = geo_oldenburg.h3.geo_to_h3(15)
    geo_oldenburg.head(1)
    geo_oldenburg_H3 = geo_oldenburg.h3.polyfill(15, explode=True)
    
    count_series = geo_oldenburg_H3.groupby('h3_polyfill').size()
    new_df = count_series.to_frame(name = 'amount').reset_index()
    new_df.sort_values(by=['amount'])



