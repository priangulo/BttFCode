Vars : [JustFM] [Base] [C] [B] [A] [A1] [A2] [B1] [B2] [L] [R] :: all;

%%

L iff ((JustFM iff Base) and (C implies JustFM) and (B implies JustFM) and (A implies JustFM) and (B iff B1 or B2) and (A iff A1 or A2) and (B1 implies A1) and (B2 implies A2) and (C implies A2));

R iff ( (JustFM iff Base)  and (C implies JustFM) and (B1 implies JustFM) and (B2 implies JustFM) and (A1 implies JustFM) and (A2 implies JustFM) and (B1 implies A1) and (B2 implies A2) and (C implies A2) and (A1 or A2));

L iff not R;

