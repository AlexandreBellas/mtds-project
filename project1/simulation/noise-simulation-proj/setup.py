#!/usr/bin/env python
# -*- encoding: utf-8 -*-
from __future__ import absolute_import
from __future__ import print_function

import io
import re
from glob import glob
from os.path import basename
from os.path import dirname
from os.path import join
from os.path import splitext

from setuptools import find_packages
from setuptools import setup


def read(*names, **kwargs):
    return io.open(
        join(dirname(__file__), *names),
        encoding=kwargs.get('encoding', 'utf8')
    ).read()


setup(
    name='nameless',
    version='0.1.0',
    packages=find_packages('src'),
    package_dir={'': 'src'},
    py_modules=[splitext(basename(path))[0] for path in glob('src/*.py')],
    include_package_data=True,
    install_requires=[
        'numpy',
        'pandas',
        'geopandas',
        #'shapely.geometry',
        'datetime',
        'pyproj',
        'h3',
        #'math',
        'functools',
        'h3pandas',
        'python-dotenv',
        #'pyspark.sql',
        #'pyspark.streaming',
    ],
    entry_points={
        'console_scripts': [
            'nameless = src.cli:main',
        ]
    },
)