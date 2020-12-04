int testFunction(int n,char c)
{
int a = 0, b = 1;
do {
    if (b == 0) {
        b++;
        continue;
    } else {
        b--;
    }
    a+=b;
} while (b > n);
for (int i = 0; i < a; i++) {
    if (i > b) {
        switch (c-10) {
            case 1:
                continue;
                break;
            case 2:
                b = 0;
                break;
            default:
                break;
        }
    } else {
        while (i < b) {
            b++;
        }
    }
}
return n + c + a + b;
}