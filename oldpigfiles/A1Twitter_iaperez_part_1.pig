Question 1.a: Why do we do this? Under what circumstances would there be duplicates?    

There would be duplicates in the case that the user repeats the same query in the same hour.


Question 1.b: In English, what is this command   doing?  

The statemente is grouping all the users that searched the same  keyword in the same hour.


Question 1.c: Why is the FLATTEN command used here? What are we really counting with COUNT($1) mean?
(Hint: see the GROUP documentation linked to above.)

Flatten is used to unnest the previous group key (ngram, hour), converting that key in two independent 
columns, then facilitates the work with this two columns. The count($1) is counting the number of tuplets 
that are stored in the bag that is the result of the GROUP statement. The bag in particular is the tuples from the relation Ngram2, 
which is the distinct elements founded by the ngramgenerator.



Question 1.d: In English, what is this command doing?    
With this command, the resulting relation will show, for each keyword, the number of times of each hour 
in a day in which that keyword was searched. 
For example:this is one of the tuplets
(sleep,{(sleep,00,1),(sleep,14,2)})
and you can interpret this like: The word "sleep" was searched 1 time at 00am bucket (00) and 2 times in the 2pm bucket (14).
