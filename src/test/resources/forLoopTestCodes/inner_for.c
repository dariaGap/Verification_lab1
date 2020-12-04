int testFunction(int n)
{
int a = 0, b = 1, c = a + b, d;
int j,k;
for (int i=0; i<n;i++) {
    for (j=0; j<i;j++) {
        c++;
        for (k=0; k<=j;k++) {
            a++;
        }
        b = c + a;
    }
    c = a + b;
    a = b + c;
    for (k=0; k<i;k++) {
        a++;
    }
}
d = n + a + b + c;
return d;
}