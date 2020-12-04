int testFunction(int n)
{
int a = 0, b = 1, c, d;
if (n>0) {
    a++;
    b = a + 1;
    if (b > n) {
        b = 10;
    }
    else {
        n = n + 2;
        return b;
        }
} else {
    n +=10;
    if (n>0) {
        if (b > a)
            b <<= 4;
    } else {
        b = a & n;
        a = a||n;
    }
    b = a;
}
c = a + b;
d = a - b;
if (c > d)
    return c;
return d;
}