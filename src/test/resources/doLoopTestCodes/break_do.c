int testFunction(int n)
{
int a = 0, b = 1;
do {
    if (b == 0) {
        b++;
        continue;
    } else {
        b--;
    }
    a+=b;
    if (a < n) {
        a = a + n;
        break;
    }
} while (b > n);
return n + a + b;
}