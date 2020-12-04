int testFunction(int n)
{
int a = 0, b = 1;
do {
    b--;
    a+=b;
} while (b > n);
return n + a + b;
}