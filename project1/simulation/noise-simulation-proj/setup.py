#!/usr/bin/env python
# -*- encoding: utf-8 -*-
import io
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
    name='simulation',
    version='0.1.0',
    packages=find_packages('simulation'),
    package_dir={'': 'simulation'},
    py_modules=[splitext(basename(path))[0] for path in glob('simulation/*.py')],
    include_package_data=True,
    install_requires=[],
    entry_points={
        'console_scripts': [
            'simulation=simulation.__main__:main',
        ]
    },
)