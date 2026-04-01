# spatial-kd-tree-query
# 2D-Tree Range Query for Point Sets

## 1. Requirement

Given a set of **N** distinct points in a 2D plane and a query rectangle with sides parallel to the coordinate axes, the objective is to identify and count all points from the set that lie strictly inside the rectangle.

The solution implements a **2D Binary Space Partitioning Tree (2D-Tree)** to optimize the spatial search, reducing the query time complexity from linear to sublinear.

---

## 2. Algorithm Description

The algorithm builds a binary tree where each node represents a point and defines a splitting line. These lines alternate between vertical and horizontal at each level of the tree.

### Phase I: Pre-processing
The input set **S** is pre-sorted to facilitate efficient median finding:
* **L1:** Points sorted ascending by **X-coordinate**.
* **L2:** Points sorted ascending by **Y-coordinate**.

### Phase II: Tree Construction
The tree is built recursively using a median-split strategy:
1. **Root Selection:** The median point from the current list is chosen (based on X if the cut is vertical, or Y if horizontal). This point becomes node **v**.
2. **Type Assignment:** A split type $T(v)$ is assigned: **VERTICAL** (splits at $x$) or **HORIZONTAL** (splits at $y$).
3. **Subdivision:** * **Left Subtree:** Points located to the left or below the splitting line.
   * **Right Subtree:** Points located to the right or above the splitting line.
4. **Alternation:** The process continues recursively, alternating the axis at each level: `splitByX → splitByY → splitByX...`

### Phase III: Range Interrogation
To find points within a search rectangle $D = [x_1, x_2] \times [y_{bottom}, y_{top}]$, the algorithm traverses the tree starting from the root:

1. **Inclusion Check:** If the current node $v$ satisfies $(x_1 \le v.x \le x_2) \land (y_{bottom} \le v.y \le y_{top})$, it is added to the result list.
2. **Pruning & Traversal:** Based on the split type of $v$:
   * If the splitting coordinate is $\ge min\_boundary$ of the rectangle $\implies$ **Search Left Subtree**.
   * If the splitting coordinate is $\le max\_boundary$ of the rectangle $\implies$ **Search Right Subtree**.

---

## 3. Complexity
* **Construction:** $O(N \log N)$ using pre-sorted lists or $O(N \log N)$ with a median-finding algorithm.
* **Query Time:** Average $O(\log N)$ for balanced trees, with a worst-case of $O(\sqrt{N})$ for range searches in 2D.
* **Space Complexity:** $O(N)$ to store the tree nodes.
