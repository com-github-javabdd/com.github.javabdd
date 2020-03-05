// This test verify the bug fix about the return value of bdd_support when it is given a constant bdd.
// Since the support of a bdd is a variable set, 'no variables' should be represented as true.

#include <iostream>
#include "bdd.h"

int main()
{
   bdd_init(256, 10000) ;
   bdd_setvarnum( 4 ) ; // choosing arbitrary number of variables

   std::cout << bdd_support( bddtrue ) << std::endl ;
   std::cout << bdd_support( bddfalse ) << std::endl ;

   bdd_done();
   
   return 0;
}
