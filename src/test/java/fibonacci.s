class Fibonacci { // comment
    pub Str " Esto es una prueba de string ";
    /* Comentario       Multilinea
     de prueba */
    pub Int suma;
    pub Int i,j;
}
impl Fibonacci {
    fn Int sucesion_fib(Int n){
        i=0; j=0; suma=0;
        while (i <= n){
            if (i == 0){
                (imprimo_numero(i));
                (imprimo_sucesion(suma)); }
            else
                if(i==1){
                    (imprimo_numero(i));
                    suma=suma+i;
                    (imprimo_sucesion(suma));}
                else{
                    (imprimo_numero(i));
                    suma=suma+j;
                    j=suma;
                    (imprimo_sucesion(suma));
                }
            (++i);
        }
    ret suma;
    }

    .(){
        i=0; // inicializo i
        j=0; // inicializo j
        suma=0; // inicializo suma
    }

    fn imprimo_numero(Int num){
        (IO.out_str("f_"));
        (IO.out_int(num));
        (IO.out_str("="));
    }

    fn imprimo_sucesion(Int s){
        // El valor es:
        (IO.out_int(s));
        (IO.out_str("\n"));
    }
}

start{
    Fibonacci fib;
    Int n;
    fib = new Fibonacci();
    n = IO.in_int();
    (IO.out_int(fib.sucesion_fib(n)));
}