# -*- coding: utf-8 -*-
"""演示数据录入：请求封装（登录 + CSRF）。

约定：ORIGIN 从 sys.argv[1] 读取，后续接口只需传相对路径。
"""
import http.cookiejar
import json
import sys
import urllib.error
import urllib.parse
import urllib.request

ORIGIN = (sys.argv[1] if len(sys.argv) > 1 else 'http://127.0.0.1:8080').rstrip('/')
USERNAME = sys.argv[2] if len(sys.argv) > 2 else ''
PASSWORD = sys.argv[3] if len(sys.argv) > 3 else ''

JAR = http.cookiejar.CookieJar()
OPENER = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(JAR))


def request(method, path, payload=None, headers=None):
    data = None
    extra = dict(headers or {})
    if payload is not None:
        data = json.dumps(payload, ensure_ascii=False).encode('utf-8')
        extra['Content-Type'] = 'application/json'
    req = urllib.request.Request(ORIGIN + path, data=data, headers=extra, method=method)
    try:
        with OPENER.open(req) as res:
            text = res.read().decode('utf-8')
            parsed = json.loads(text) if text else None
            return (parsed or {}).get('data')
    except urllib.error.HTTPError as err:
        detail = err.read().decode('utf-8', 'replace')
        raise SystemExit('{} {} -> {} {}'.format(method, path, err.code, detail))


def csrf():
    return request('GET', '/api/admin/auth/csrf')['token']


def get(path):
    return request('GET', path)


def post(path, payload=None):
    token = csrf()
    return request('POST', path, payload, {'X-CSRF-TOKEN': token})


def put(path, payload=None):
    token = csrf()
    return request('PUT', path, payload, {'X-CSRF-TOKEN': token})


def login():
    token = csrf()
    data = urllib.parse.urlencode({'username': USERNAME, 'password': PASSWORD}).encode('utf-8')
    req = urllib.request.Request(
        ORIGIN + '/api/admin/auth/login',
        data=data,
        headers={
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': token,
        },
        method='POST',
    )
    try:
        with OPENER.open(req) as res:
            return json.loads(res.read().decode('utf-8'))['data']
    except urllib.error.HTTPError as err:
        raise SystemExit('登录失败: ' + err.read().decode('utf-8', 'replace'))
