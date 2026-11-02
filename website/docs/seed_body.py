# -*- coding: utf-8 -*-
"""演示数据录入主体：创建并发布分类与内容，再把首页区块指向已发布内容。

用法：python seed_body.py <baseUrl> <username> <password>
"""
import json
import sys

from seed_lib import login, get, post
from seed_all import CATEGORIES, SOLUTIONS, CASES, PRODUCTS, NEWS, CUSTOMERS

MEDIA_ID = 1


def create_and_publish(content_type, draft):
    created = post('/api/admin/contents', {'contentType': content_type, 'draft': draft})
    if not created or 'content' not in created:
        raise SystemExit('创建失败 {}: {}'.format(content_type, created))
    content = created['content']
    post(f"/api/admin/contents/{content['id']}/publish", {
        'expectedLockVersion': content['lockVersion'],
        'expectedDraftVersion': content['draftVersion'],
    })
    return content['id']


def main():




    who = login()
    print('登录成功:', who.get('username'), who.get('role'), flush=True)

    cat_ids = {}
    existing = {item['categoryType'] + ':' + item['name']: item
                for item in get('/api/admin/categories?pageSize=50')['items']}
    print('--- 分类 ---', flush=True)
    for ctype, name, order in CATEGORIES:
        key = ctype + ':' + name
        found = existing.get(key)
        if found:
            cid = found['id']
            if found['status'] != 'PUBLISHED':
                post('/api/admin/categories/{}/publish'.format(cid), {
                    'expectedLockVersion': found['lockVersion'],
                    'expectedDraftVersion': found['draftVersion'],
                })
        else:
            cid = post('/api/admin/categories', {
                'categoryType': ctype,
                'name': name,
                'sortOrder': order,
            })
            content = get('/api/admin/categories/' + str(cid))
            post('/api/admin/categories/{}/publish'.format(cid), {
                'expectedLockVersion': content['lockVersion'],
                'expectedDraftVersion': content['draftVersion'],
            })
        cat_ids[key] = cid
        print('已发布分类', ctype, name, 'id=' + str(cid), flush=True)

    ids = {'PRODUCT': [], 'SOLUTION': [], 'CASE': [], 'NEWS': [], 'CUSTOMER': []}

    print('--- 产品 ---', flush=True)
    for index, item in enumerate(PRODUCTS):
        draft = dict(item)
        draft['coverMediaId'] = MEDIA_ID
        draft.setdefault('coverAlt', item['title'][:20])
        draft['categoryId'] = cat_ids['PRODUCT:' + ('数据采集' if index == 0 else '能源管理')]
        ids['PRODUCT'].append(create_and_publish('PRODUCT', draft))

    print('--- 方案 ---', flush=True)
    for item in SOLUTIONS:
        draft = dict(item)
        draft['categoryId'] = cat_ids['SOLUTION:' + item['industry']]
        draft['coverMediaId'] = MEDIA_ID
        draft.setdefault('coverAlt', item['title'][:20])
        draft.setdefault('relatedProductIds', [])
        ids['SOLUTION'].append(create_and_publish('SOLUTION', draft))

    print('--- 案例 ---', flush=True)
    for item in CASES:
        draft = dict(item)
        draft['categoryId'] = cat_ids[item['catKey']]
        draft.pop('catKey', None)
        draft['coverMediaId'] = MEDIA_ID
        draft.setdefault('coverAlt', item['title'][:20])
        draft.setdefault('relatedProductIds', [])
        draft.setdefault('relatedSolutionIds', [])
        draft.setdefault('bodyHtml', '<p>{}</p>'.format(item['summary']))
        draft.setdefault('industry', item['catKey'].split(':')[1])
        ids['CASE'].append(create_and_publish('CASE', draft))

    print('--- 新闻 ---', flush=True)
    for item in NEWS:
        title, summary, display_time, author, source_name, cat_key = item
        draft = {
            'title': title,
            'summary': summary,
            'coverMediaId': MEDIA_ID,
            'coverAlt': title[:20],
            'categoryId': cat_ids[cat_key],
            'sortOrder': (len(ids['NEWS']) + 1) * 10,
            'displayTime': display_time,
            'author': author,
            'sourceName': source_name,
            'sourceUrl': '',
            'bodyHtml': '<p>{}</p><h2>要点</h2><ul><li>问题识别</li><li>实施步骤</li><li>效果验证</li></ul>'.format(summary),
            'seo': {'title': title},
        }
        ids['NEWS'].append(create_and_publish('NEWS', draft))

    print('--- 客户标识 ---', flush=True)
    for index, name in enumerate(CUSTOMERS):
        draft = {
            'title': name,
            'logoMediaId': MEDIA_ID,
            'logoAlt': name + ' Logo',
            'websiteUrl': '',
            'sortOrder': (index + 1) * 10,
        }
        ids['CUSTOMER'].append(create_and_publish('CUSTOMER', draft))

    print('IDS=' + json.dumps(ids), flush=True)
    print('DONE', flush=True)


main()
