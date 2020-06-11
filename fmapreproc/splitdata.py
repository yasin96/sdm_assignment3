from typing import Tuple

import pandas
import sys
import numpy as np
import os
import json


def split_ids(path):
    tracks = pandas.read_csv(
        path,
        index_col=0,
        header=[0, 1]
    )
    small = tracks[tracks['set', 'subset'] == 'small']
    train = small[tracks['set', 'split'] == 'training']
    test = small[tracks['set', 'split'] == 'test']
    validate = small[tracks['set', 'split'] == 'validation']
    return small, np.array(train.index).reshape(-1), np.array(test.index).reshape(-1), np.array(validate.index).reshape(
        -1),


def split_features(path, train_ids, test_ids, validate_ids):
    features = pandas.read_csv(
        path,
        index_col=0,
        header=[0, 1, 2]
    )
    train = features.loc[train_ids]
    test = features.loc[test_ids]
    validate = features.loc[validate_ids]
    return train, test, validate


def store_to_json(df, path):
    with open(path, 'w+') as file:
        file.write("{")
        n = len(df.index)
        for i, item_id in enumerate(df.index):
            file.write(f'"{item_id}": ')
            value = df.loc[item_id]
            if isinstance(value, pandas.Series):
                file.write(df.loc[item_id].to_json())
            else:
                file.write(json.dumps(value))
            if i < n - 1:
                file.write(",")
        file.write("}")


if __name__ == '__main__':
    if len(sys.argv) != 2:
        root = "../fma_metadata\\"
    else:
        root = sys.argv[1]
    small, train_ids, test_ids, validate_ids = split_ids(root + "tracks.csv")
    train, test, validate = split_features(root + "features.csv", train_ids, test_ids, validate_ids)
    print("train")
    print(train.shape)
    # print(train.describe())
    print("test")
    print(test.shape)
    # print(test.describe())
    print("validate")
    print(validate.shape)
    # print(validate.describe())
    split_root = root + "\\split\\"
    if not os.path.exists(split_root):
        os.makedirs(split_root)
    store_to_json(small.track['genre_top'], split_root + "genres.json")
    store_to_json(train, split_root + "train.json")
    store_to_json(test, split_root + "test.json")
    store_to_json(validate, split_root + "validate.json")
