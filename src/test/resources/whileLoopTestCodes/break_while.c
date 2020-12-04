int testFunction(int n)
{
int a = 0, b = 1;
while (b > n) {
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
}
return n + a + b;
}