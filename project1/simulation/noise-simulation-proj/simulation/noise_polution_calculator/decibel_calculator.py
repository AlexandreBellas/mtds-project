import math
from functools import reduce


def decibel_calculator(array_of_noise):
    array_of_noise = [math.pow(10, i / 10) for i in array_of_noise]
    return 10 * math.log10(reduce(lambda x, y: x + y, array_of_noise))
