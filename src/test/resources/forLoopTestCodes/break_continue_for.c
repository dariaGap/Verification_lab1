int testFunction(int n)
{
int a = 0, b = 1, c = 0, d;
for (int i=0; i<n;i++) {
    b = c + b;
    if (b > c) {
        b--;
        continue;
    }
    if (b > a) {
        a++;
        break;
    }
}
d = n + a + b;
return d;
}