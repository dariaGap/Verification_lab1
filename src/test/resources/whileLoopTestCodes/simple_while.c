int testFunction(int n)
{
int a = 0, b = 1, d;
while (b > n) {
    b--;
    a+=b;
}
d = n + a + b;
return d;
}