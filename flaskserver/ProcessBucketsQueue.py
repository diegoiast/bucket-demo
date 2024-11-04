#! /usr/bin/python3

from collections import deque

class ProcessBucketsQueue:
    def __init__(self):
        self.buckets = []
        self.current_bucket = []
        self.current_sum = 0
        self.buffer = deque()
        self.item_count = 0

    def add_number(self, value):
        number = float(value)
        self.item_count += 1
        if not 1.01 <= number <= 3:
            raise ValueError(f"Number must be between 1.01 and 3, value was {value}")

        number = float(value)
        self._process_buffer()
        self._process_number(number)

    def _process_buffer(self):
        while self.buffer and self.current_sum < 3.0:
            num = self.buffer.popleft()
            if self.current_sum + num <= 3.0:
                self.current_bucket.append(num)
                self.current_sum += num
            else:
                self.buffer.appendleft(num)
                break

    def _process_number(self, number):
        if self.current_sum + number <= 3.0:
            self.current_bucket.append(number)
            self.current_sum += number
        else:
            if self.current_bucket:
                self.buckets.append(self.current_bucket)
                self.current_bucket = []
                self.current_sum = 0
            self.buffer.append(number)

    def _get_current_state(self):
        return {
            "buckets": self.buckets.copy(),
            "current": self.current_bucket.copy(),
            "buffer": list(self.buffer)
        }

    def finalize(self):
        self._process_buffer()
        if self.current_bucket:
            self.buckets.append(self.current_bucket)
        while self.buffer:
            self.buckets.append([self.buffer.popleft()])
        result = self.buckets.copy()
        self.__init__()  
        return result

if __name__ == '__main__':
    processor = ProcessBucketsQueue()
    numbers = [2.5, 1.4, 1.01, 1.5, 2.0, 1.8, 1.2, 2.7, 1.9,  2.1, 2.99]
    for num in numbers:
        processor.add_number(str(num))

    item_count = processor.item_count
    result = processor.finalize()
    print(f"\nFinal grouped numbers: {len(result)}")
    for bucket in result:
        print(f"{bucket} (sum: {sum(bucket):.2f})")

    print(f"\nTotal items processed: {item_count}")

