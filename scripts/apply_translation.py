# -*- coding: utf-8 -*-
"""把日文数据回填为中文：生成 data/pose-zh.json / meta-zh.json / translations.csv

用法（在仓库根目录）: python3 scripts/apply_translation.py
"""
import csv
import json
import os
import sys
from collections import Counter

import i18n

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "data")

with open(os.path.join(DATA, "pose.json"), encoding="utf-8") as f:
    data = json.load(f)
with open(os.path.join(DATA, "meta.json"), encoding="utf-8") as f:
    meta = json.load(f)

maps = {
    "系統": i18n.SYSTEM,
    "分類": i18n.CATEGORY,
    "アングル": i18n.ANGLE,
    "ポーズ名": i18n.POSE,
}

missing = {k: sorted({x.get(k) for x in data} - set(m)) for k, m in maps.items()}
bad = {k: v for k, v in missing.items() if v}
if bad:
    print("!! 缺失译词：")
    for k, v in bad.items():
        for s in v:
            print(f"  [{k}] {s}")
    sys.exit(1)

# 中文键名版本
out = []
for x in data:
    out.append({
        "id": x["id"],
        "体系": i18n.SYSTEM[x["系統"]],
        "姿势编号": x["ポーズ番号"],
        "分类": i18n.CATEGORY[x["分類"]],
        "姿势名称": i18n.POSE[x["ポーズ名"]],
        "角度": i18n.ANGLE[x["アングル"]],
        "缩略图": x["サムネ"],
        "原图": x["原寸"],
        "检索": f'{x["id"]} {i18n.SYSTEM[x["系統"]]}{i18n.CATEGORY[x["分類"]]}{i18n.POSE[x["ポーズ名"]]}{i18n.ANGLE[x["アングル"]]}',
    })
with open(os.path.join(DATA, "pose-zh.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)

with open(os.path.join(DATA, "meta-zh.json"), "w", encoding="utf-8") as f:
    json.dump({"体系分类": [i18n.SYSTEM[s] for s in meta["lines"]]}, f, ensure_ascii=False, indent=2)

# 保留日文原值的双语表（前端可做 hover 对照）
with open(os.path.join(DATA, "translations.csv"), "w", encoding="utf-8-sig", newline="") as f:
    w = csv.writer(f)
    w.writerow(["字段", "日文原文", "中文译文", "出现次数"])
    for k in ["系統", "分類", "角度", "ポーズ名"]:
        field = {"系統": "系統", "分類": "分類", "角度": "アングル", "ポーズ名": "ポーズ名"}[k]
        cnt = Counter(x[field] for x in data)
        for jp in sorted(maps[field]):
            w.writerow([k, jp, maps[field][jp], cnt[jp]])

print("OK 记录数:", len(out))
print("唯一译词:", sum(len(m) for m in maps.values()))
