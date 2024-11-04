#! /usr/bin/python3

from flask import Flask, jsonify, request
from collections import deque
# import asyncio

from ProcessBucketsQueue import ProcessBucketsQueue
from ProcessBucketsAccumulator import ProcessBucketsAccumulator

app = Flask(__name__)

processor = ProcessBucketsQueue()

@app.route('/init/', methods=['GET'])
def init():
    global processor
    processor = ProcessBucketsQueue()
    return jsonify({"message": "ValueProcessor initialized"}), 200

@app.route('/init/v1', methods=['GET'])
def initv1():
    global processor
    processor = ProcessBucketsQueue()
    return jsonify({"message": "ValueProcessor initialized"}), 200

@app.route('/init/v2', methods=['GET'])
def initv2():
    global processor
    processor = ProcessBucketsAccumulator()
    return jsonify({"message": "ValueProcessor initialized"}), 200

@app.route('/add_number/<value>', methods=['POST'])
async def add_number(value):
    processor.add_number(value)
    return jsonify({"result": "OK"}), 200

@app.route('/finalize/', methods=['GET'])
async def finalize():
    item_count = processor.item_count
    trips = processor.finalize()
    return jsonify({"trips": trips, "item_count": item_count}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)