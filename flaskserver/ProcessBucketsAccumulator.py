#! /usr/bin/python3

from collections import deque

class ProcessBucketsAccumulator:
    def __init__(self):
        self.numbers = []
        self.item_count = 0

    def add_number(self, value):
        number = float(value)
        if not 1.01 <= number <= 3:
            raise ValueError(f"Number must be between 1.01 and 3, value was {value}")

        self.item_count += 1
        self.numbers.append(value)

    def finalize(self):
        sorted_numbers = sorted(self.numbers, reverse=True)
        
        result = []
        current_group = []
        current_sum = 0

        for num in sorted_numbers:
            if current_sum + num <= 3:
                current_group.append(num)
                current_sum += num
            else:
                if current_group:
                    result.append(current_group)
                current_group = [num]
                current_sum = num

        if current_group:
            result.append(current_group)
        
        self.numbers = []
        self.item_count = 0

        return result

    def get_all_processed_numbers(self):
        return self.numbers.copy()

if __name__ == '__main__':
    processor = ProcessBucketsAccumulator()
    numbers = [2.5, 1.4, 1.01, 1.5, 2.0, 1.8, 1.2, 2.7, 1.9,  2.1, 2.99]
    for num in numbers:
        processor.add_number(num)
        # print(f"Added {num}. ")

    item_count = processor.item_count
    result = processor.finalize()
    print(f"\nFinal grouped numbers: {len(result)}")
    for group in result:
        print(f"{group} (sum: {sum(group):.2f})")

    print(f"\nTotal items processed: {item_count}")
