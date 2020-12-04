int testFunction(int n)
{
int a = 0, b = 1, c = a + b;
int i = 0, j = 0;
do {
    i++;
    do {
        j++;
        c++;
        if (c > a)
            continue;
        b = c + a;
        if (b > a)
            break;
    } while(j<i);
    c = a + b;
    if (c > a)
        continue;
    else
        a = b + c;
} while(i<n);
return a + b + c;
}