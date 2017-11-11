//temporary class to help for creating codes for huffman algorithm
class Node {
    Node left, right;
    double value;
    int b;

    public Node(double value, int b) {
        this.value = value;
        this.b = b;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.value = left.value + right.value;
        if (left.value < right.value) {
            this.right = right;
            this.left = left;
        } else {
            this.right = left;
            this.left = right;
        }
    }
}