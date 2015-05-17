#! /usr/bin/env python3
import glob
from collections import defaultdict
from functools import partial


def read_csv(path, delim, conv=None):

    with open(path) as f:
        lines = f.read().split('\n')

    lines = [l.split(delim) for l in lines]
    if lines[-1][0] == '':
        del lines[-1]
    headers = lines[0]
    headers_dict = {headers[i]: i for i in range(len(headers))}

    def get(key, line):
        lk = headers_dict[key]
        if conv:
            return conv[key](line[lk])
        return line[lk]

    rdata = []
    for line in lines[1:]:
        rdata.append(partial(get, line=line))

    return rdata

def write_csv(path,delim,lines,headers=None):

    with open(path,'w') as f:
        if headers:
            f.write(delim.join(headers)+'\n')
        for line in lines:
            f.write(delim.join(str(x) for x in line)+'\n')


def normalize_uprice_samples(path):
    data = read_csv(path, '\t',
                        {'volume': int,
                         'unit_price': float,
                         'quality': int})
    data_d= {}
    for row in data:
        key = (row('volume'),row('quality'))        
        val = row('unit_price')
        if key in data_d and data_d[key] != val:
            print(key)
            raise Exception('unequal data')
        data_d[key] = row('unit_price')

    data_items = list(data_d.items())
    data_items.sort()
    data_items = [ (x[0][0],x[0][1],x[1]) for x in data_items]
    write_csv('n_uprice.csv','\t',data_items,['volume','quality','unit_price'])
    
def normalize_sold_ratio_samples(path):
    paths = glob.glob(path)
    for path in paths:
        _normalize_sold_ratio_samples(path)
def _normalize_sold_ratio_samples(path):
    data = read_csv(path, '\t',
                        {'volume': int,
                         'quality': int,
                         'tv' : int,
                         'internet' :int,
                         'warehouse':int,
                         'price' : int,
                         'sold_num' :int,
                         'sold_ratio':float,
                         'income' : int,
                         'return_rate' :float,
                         'unit_price' : float
                         })
    data_d= defaultdict(lambda : defaultdict(int))
    for row in data:
        key = (row('quality'),row('tv'),row('internet'),row('warehouse'),row('price'))
        val = round(row('sold_ratio'),3)
        data_d[key][val] += 1

    # for key in data_d.keys():
    #     cd = data_d[key]
    #     if len(cd) > 1:
    #         f = False
    #         for ik in cd.keys():
    #             if cd[ik] > 1:
    #                 print('wow')
    #                 f = True
    #         if not f:
    #             print_counter(cd)

    rows = []
    for k in data_d:
        h = 0
        dd = data_d[k]
        for k2 in dd:
            if h == 0 :
                h=k2
            elif h < dd[k2]:
                h=k2
        row = k + (h,)
        rows.append(row)
    rows.sort()

    write_csv('percsold.csv','\t',rows,['quality','tv','internet','warehouse','price','sold_ratio'])

def print_counter(ct):
    for k in ct:
        print(ct[k],k)

normalize_uprice_samples('./data/solid/unit_price.csv')
# normalize_sold_ratio_samples('./data/solid/fin_genetic.csv')
