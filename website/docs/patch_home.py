# -*- coding: utf-8 -*-
"""把首页的 services / cases / customers / news 区块指向已发布的真实内容。

用法：python patch_home.py <baseUrl> <username> <password> <caseIds> <customerIds> <productIds>
例：python patch_home.py http://127.0.0.1:8080 admin pwd 6,7,8 13,14,15,16,17,18 2,3
"""
import json
import sys

from seed_lib import login, get, post, put


def main():
    case_ids = [int(x) for x in sys.argv[4].split(',') if x]
    customer_ids = [int(x) for x in sys.argv[5].split(',') if x]
    product_ids = [int(x) for x in sys.argv[6].split(',') if x]

    who = login()
    print('登录成功:', who.get('username'), who.get('role'), flush=True)

    home = get('/api/admin/configs/HOME')
    draft = dict(home.get('draft') or {})

    services = dict(draft.get('services') or {})
    groups = list(services.get('groups') or [])
    if groups:
        group = dict(groups[0])
        group['contentType'] = 'PRODUCT'
        group['contentIds'] = product_ids
        groups[0] = group
        services['groups'] = groups
        draft['services'] = services


    cases = dict(draft.get('cases') or {})
    cases['enabled'] = True
    cases['contentIds'] = case_ids
    draft['cases'] = cases

    customers = dict(draft.get('customers') or {})
    customers['enabled'] = True
    customers['contentIds'] = customer_ids
    draft['customers'] = customers

    news = dict(draft.get('news') or {})
    news['enabled'] = True
    news['mode'] = 'LATEST'
    news['limit'] = 4
    news['contentIds'] = []
    draft['news'] = news

    put('/api/admin/configs/HOME/draft', {
        'expectedLockVersion': home['lockVersion'],
        'draft': draft,
    })

    home = get('/api/admin/configs/HOME')
    post('/api/admin/configs/HOME/publish', {
        'expectedLockVersion': home['lockVersion'],
        'expectedDraftVersion': home['draftVersion'],
    })

    print('首页已发布，cases/customers/news 指向真实内容', flush=True)


main()
