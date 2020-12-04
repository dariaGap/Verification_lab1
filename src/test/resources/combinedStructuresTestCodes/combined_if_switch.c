int testFunction(int n,int i)
{
int b = 10;
if (i < b) {
     switch (n) {
        case 0:
            i++;
            break;
        default:
            i--;
            break;
     }
}
else {
     switch (n) {
        case 0:
            b++;
            break;
        default:
            b--;
            break;
     }
}
return n + i + b;
}