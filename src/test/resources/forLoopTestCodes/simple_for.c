int testFunction(int n)
{
int a = 0, b = 1, c = a + b, d;
for (int i=0; i<n;i++) {
    b = c + b;
    c = a + b;
    a = b + c + i;
}
for (i=0; i<n;i=i+1) {
    a++;
}
d = n + a + b;
return d;
}