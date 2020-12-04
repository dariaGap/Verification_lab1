int testFunction(int n)
{
int a = 0, b = 1, c = a + b, d;
int j;
for (int i=0; i<n;i++) {
    for (j=0; j<n;j++) {
        c++;
        if (c > a)
            continue;
        b = c + a;
        if (b > a)
            break;
    }
    c = a + b;
    if (c > a)
        continue;
    else
        a = b + c;
}
d = a + b + c;
return d;
}