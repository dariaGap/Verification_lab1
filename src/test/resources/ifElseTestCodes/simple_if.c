int testFunction(int n)
{
int a = 0, b = 1, c, d;
if (n > 0) {
    c = a + 1;
    d = b + 1;
} else {
    c = a - 1;
    d = b - 1;
}
if (c > d)
    return c;
return d;
}