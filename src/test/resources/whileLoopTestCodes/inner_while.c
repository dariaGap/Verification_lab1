int testFunction(int n)
{
int a = 0, b = 1, c;
c = a + b;
while (b > n) {
    while (a<n)
        a++;
    b--;
    a+=b;
    while (c <= 100)
        c+=a+b;
}
return n + a + b + c;
}