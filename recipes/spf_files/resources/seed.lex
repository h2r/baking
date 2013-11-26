ingredient :- N : ingredient:<in,t>
ingredients :- N : ingredient:<in,t>
produced ingredient :-N : produced_ingredient:<p,t>
container :-N : container:<c,t>
containers :-N : container:<c,t>
utensil :-N : utensil:<u,t>
utensils :-N : utensil:<u,t>
space :-N : space:<s,t>
measurement unit :-N : measurement_unit:<m,t>
measurement units :-N : measurement_unit:<m,t>
// state :- N : state:<s,t>
// states :- N : state:<s,t>
// river :- N : river:<r,t>
// rivers :- N : river:<r,t>
// cities :- N : city:<c,t>
// towns :- N : town:<lo,t>
//capital :- N : capital:<s,c>
// area :- N : place:<p,t>

//named :- PP/NP : (lambda $0:e (lambda $1:e (named:<e,<n,t>> $1 $0)))
//of :- PP/NP : (lambda $0:e (lambda $1:e (named:<e,<n,t>> $1 $0)))

//determiners
the :- N/N : (lambda $0:<e,t> $0)
is :- N/N : (lambda $0:<e,t> $0)
a :- N/N : (lambda $0:<e,t> $0)
of :- N/N : (lambda $0:<e,t> $0)
the :- NP/NP : (lambda $0:e $0)
is :- NP/NP : (lambda $0:e $0)
of :- NP/NP : (lambda $0:e $0)
the :- NP/N : (lambda $0:<e,t> (the:<<e,t>,e> $0))

that :- PP/(S\NP) : (lambda $0:<e,t> $0)
that :- PP/(S/NP) : (lambda $0:<e,t> $0)
which :- PP/(S\NP) : (lambda $0:<e,t> $0)
which :- PP/(S/NP) : (lambda $0:<e,t> $0)
are :- PP/PP : (lambda $0:<e,t> $0)

are :- (N\N)/N : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
are :- (S\NP)/PP : (lambda $0:<e,t> $0)
does :- (S/NP)/(S/NP) : (lambda $0:<e,t> $0)
does :- (S\NP)/(S\NP) : (lambda $0:<e,t> $0)
// what state is dallas in
//is :- ((S\NP)/(PP/NP))/NP : (lambda $0:e (lambda $1:<e,<e,t>> (lambda $2:e ($1 $2 $0))))
is :- (S/NP)/(S/NP) : (lambda $0:<e,t> $0)
have :- (S/NP)/(S/NP) : (lambda $0:<e,t> $0)
is :- (S\NP)/(S\NP) : (lambda $0:<e,t> $0)
are there :- S\NP : (lambda $0:e true:t)
// is :- (S\NP)/NP : (lambda $0:e (lambda $1:e (equals:<e,<e,t>> $1 $0)))

// negation
not :- N/N : (lambda $0:<e,t> (lambda $1:e (not:<t,t> ($0 $1))))
not :- PP/PP : (lambda $0:<e,t> (lambda $1:e (not:<t,t> ($0 $1))))
do not :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:e (not:<t,t> ($0 $1))))
no :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:e (not:<t,t> ($0 $1))))
//excluding :- PP/NP : (lambda $0:e (lambda $1:e (not:<t,t> (equals:<e,<e,t>> $1 $0))))
//no :- (S\NP)\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (not:<t,t> ($1 (a:<<e,t>,e> $0) $2)))))

// empty sentence modifier
tell me :- S/S : (lambda $0:<e,t> $0)
can you  :- S/S : (lambda $0:<e,t> $0)
please  :- S/S : (lambda $0:<e,t> $0)
please :- S\S : (lambda $0:e $0)
please :- S\S : (lambda $0:t $0)
in feet :- S\S : (lambda $0:e $0)
in meters :- S\S : (lambda $0:e $0)
is :- S/S : (lambda $0:t $0)

// quantifier
//largest :- NP/N : (lambda $0:<e,t> (argmax:<<e,t>,<<e,i>,e>> $0 size:<lo,i>))
//largest :- NP/N/PP : (lambda $0:<e,e> (lambda $1:<e,t> (lambda $2:<e,t> (argmax:<<e,t>,<<e,i>,e>> (lambda $3:e (and:<t*,t> ($1 $3) ($2 $3))) size:<lo,i>))))
//smallest :- NP/N : (lambda $0:<e,t> (argmin:<<e,t>,<<e,i>,e>> $0 size:<lo,i>))
//smallest :- NP/N/PP : (lambda $0:<e,i> (lambda $1:<e,t> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> (lambda $3:e (and:<t*,t> ($1 $3) ($2 $3))) size:<lo,i>))))
//longest :- NP/N : (lambda $0:<e,t> (argmax:<<e,t>,<<e,i>,e>> $0 len:<r,i>))
//shortest :- NP/N : (lambda $0:<e,t> (argmin:<<e,t>,<<e,i>,e>> $0 len:<r,i>))
//highest :- NP/N : (lambda $0:<e,t> (argmax:<<e,t>,<<e,i>,e>> $0 elevation:<lo,i>))
//lowest :- NP/N : (lambda $0:<e,t> (argmin:<<e,t>,<<e,i>,e>> $0 elevation:<lo,i>))
is :- (NP\N)/(NP/N)  : (lambda $0:<<e,t>,e> $0)
are  :- (NP\N)/(NP/N)  : (lambda $0:<<e,t>,e> $0)
with  :- (NP\N)/(NP/N)  : (lambda $0:<<e,t>,e> $0)

// what is the most populated state
most :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmax:<<e,t>,<<e,i>,e>> $1 $0)))
highest :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmax:<<e,t>,<<e,i>,e>> $1 $0)))
biggest :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmax:<<e,t>,<<e,i>,e>> $1 $0)))
largest :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmax:<<e,t>,<<e,i>,e>> $1 $0)))
largest :- (NP/N)/PP/N : (lambda $0:<e,e> (lambda $1:<e,t> (lambda $2:<e,t> (argmax:<<e,t>,<<e,i>,e>> (lambda $3:e (and:<t*,t> ($1 $3) ($2 $3))) $0))))
lowest :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmin:<<e,t>,<<e,i>,e>> $1 $0)))
sparsest :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmin:<<e,t>,<<e,i>,e>> $1 $0)))
smallest :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmin:<<e,t>,<<e,i>,e>> $1 $0)))
smallest :- (NP/N)/PP/N : (lambda $0:<e,e> (lambda $1:<e,t> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> (lambda $3:e (and:<t*,t> ($1 $3) ($2 $3))) $0))))
least :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (argmin:<<e,t>,<<e,i>,e>> $1 $0)))
// largest city by population
largest :- (NP/N)/N : (lambda $0:<e,t> (lambda $1:<e,e> (argmax:<<e,t>,<<e,i>,e>> $0 $1)))
by :- N/N : (lambda $0:<e,e> $0)

// area of all of the states combined
//combined :- (NP\N)\N : (lambda $0:<e,t> (lambda $1:<e,e> (sum:<<e,t>,<<e,i>,i>> $0 $1)))
// total population of ...
//total :- (NP/N)/N : (lambda $0:<e,e> (lambda $1:<e,t> (sum:<<e,t>,<<e,i>,i>> $1 $0)))

// what river traverses the most states
//the most :- NP\N\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmax:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//most :- NP\N\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmax:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//fewest :- NP\N\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//least :- NP\N\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//the least :- NP\N\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))

//the most :- NP\N\(PP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmax:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//most :- NP\N\(PP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmax:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//fewest :- NP\N\(PP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//least :- NP\N\(PP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//the least :- NP\N\(PP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (argmin:<<e,t>,<<e,i>,e>> $2 (lambda $3:e (count:<<e,t>,i> (lambda $4:e (and:<t*,t> ($0 $4) ($1 $4 $3)))))))))
//at least one :- (S\NP)\(S\NP/NP)/N : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (>:<i,<i,t>> (count:<<e,t>,i> (lambda $3:e (and:<t*,t> ($0 $3) ($1 $3 $2)))) 0:i))))

