int testFunction(int n)
{
int a = 0, b = 1, c;
c = a + b;
do {
    do
        a++;
    while (a<n);
    b--;
    a+=b;
    do
        c+=a+b;
    while (c <= 100);
} while (b > n);
return n + a + b + c;
}