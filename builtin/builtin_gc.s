	.file	"builtin_gc.c"
	.option nopic
	.attribute arch, "rv32i2p0_a2p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
	.align	2
	.globl	__mx_builtin_gc_r_sp
	.type	__mx_builtin_gc_r_sp, @function
__mx_builtin_gc_r_sp:
 #APP
# 9 "builtin_gc.c" 1
	mv a0, sp
# 0 "" 2
 #NO_APP
	ret
	.size	__mx_builtin_gc_r_sp, .-__mx_builtin_gc_r_sp
	.align	2
	.globl	__mx_builtin_gc_find
	.type	__mx_builtin_gc_find, @function
__mx_builtin_gc_find:
	lui	a5,%hi(__mx_builtin_gc_en)
	lw	a5,%lo(__mx_builtin_gc_en)(a5)
	mv	a6,a0
	addi	a5,a5,-1
	blt	a5,zero,.L13
	lui	a1,%hi(__mx_builtin_gc_a)
	li	a7,-1
	li	a4,0
	addi	a1,a1,%lo(__mx_builtin_gc_a)
.L9:
	add	a0,a5,a4
	srai	a0,a0,1
	slli	a3,a0,2
	add	a2,a1,a3
	lw	a2,0(a2)
	bgt	a2,a6,.L5
.L14:
	addi	a4,a0,1
	blt	a5,a4,.L7
	mv	a7,a0
	add	a0,a5,a4
	srai	a0,a0,1
	slli	a3,a0,2
	add	a2,a1,a3
	lw	a2,0(a2)
	ble	a2,a6,.L14
.L5:
	addi	a5,a0,-1
	bge	a5,a4,.L9
	li	a5,-1
	beq	a7,a5,.L13
	mv	a0,a7
	slli	a3,a7,2
.L7:
	add	a3,a1,a3
	lw	a5,0(a3)
	beq	a5,a6,.L3
	addi	a5,a5,4
	bne	a5,a6,.L13
.L3:
	ret
.L13:
	li	a0,-1
	ret
	.size	__mx_builtin_gc_find, .-__mx_builtin_gc_find
	.align	2
	.globl	__mx_builtin_gc_abs
	.type	__mx_builtin_gc_abs, @function
__mx_builtin_gc_abs:
	srai	a5,a0,31
	xor	a0,a5,a0
	sub	a0,a0,a5
	ret
	.size	__mx_builtin_gc_abs, .-__mx_builtin_gc_abs
	.align	2
	.globl	__mx_builtin_gc
	.type	__mx_builtin_gc, @function
__mx_builtin_gc:
	addi	sp,sp,-2032
	sw	ra,2028(sp)
	sw	s0,2024(sp)
	sw	s1,2020(sp)
	sw	s2,2016(sp)
	sw	s3,2012(sp)
	sw	s4,2008(sp)
	sw	s5,2004(sp)
	sw	s6,2000(sp)
	sw	s7,1996(sp)
	addi	sp,sp,-2032
 #APP
# 9 "builtin_gc.c" 1
	mv t5, sp
# 0 "" 2
 #NO_APP
	lui	a5,%hi(__mx_builtin_gc_sps)
	lw	t6,%lo(__mx_builtin_gc_sps)(a5)
	lui	s1,%hi(__mx_builtin_gc_en)
	lw	t4,%lo(__mx_builtin_gc_en)(s1)
	bleu	t6,t5,.L28
	li	a5,4096
	addi	a5,a5,-80
	lui	a6,%hi(__mx_builtin_gc_a)
	lui	t0,%hi(__mx_builtin_gc_sz)
	li	t2,-4096
	add	a5,a5,sp
	li	t3,0
	addi	t1,t4,-1
	li	s0,-1
	addi	a6,a6,%lo(__mx_builtin_gc_a)
	addi	t0,t0,%lo(__mx_builtin_gc_sz)
	add	t2,a5,t2
.L27:
	lw	a0,0(t5)
	blt	t1,zero,.L20
	mv	a3,t1
	li	a7,-1
	li	a4,0
.L25:
	add	a5,a3,a4
	srai	a5,a5,1
	slli	a2,a5,2
	add	a1,a6,a2
	lw	a1,0(a1)
	blt	a0,a1,.L21
.L57:
	addi	a4,a5,1
	bgt	a4,a3,.L23
	mv	a7,a5
	add	a5,a3,a4
	srai	a5,a5,1
	slli	a2,a5,2
	add	a1,a6,a2
	lw	a1,0(a1)
	bge	a0,a1,.L57
.L21:
	addi	a3,a5,-1
	bge	a3,a4,.L25
	beq	a7,s0,.L20
	mv	a5,a7
	slli	a2,a7,2
.L23:
	add	a4,a6,a2
	lw	a4,0(a4)
	beq	a0,a4,.L26
	addi	a4,a4,4
	bne	a0,a4,.L20
.L26:
	add	a2,t0,a2
	lw	a4,0(a2)
	ble	a4,zero,.L20
	addi	t3,t3,1
	slli	a3,t3,2
	neg	a4,a4
	add	a3,t2,a3
	sw	a4,0(a2)
	sw	a5,84(a3)
.L20:
	addi	t5,t5,4
	bltu	t5,t6,.L27
	beq	t3,zero,.L28
	li	a4,4096
	addi	a5,a4,-80
	li	t2,-4096
	add	a5,a5,sp
	add	t5,a5,t2
	addi	a5,a4,-80
	lui	s2,%hi(__mx_builtin_gc_a)
	lui	t0,%hi(__mx_builtin_gc_sz)
	add	a5,a5,sp
	addi	s0,s2,%lo(__mx_builtin_gc_a)
	addi	t5,t5,88
	li	t6,1
	addi	t0,t0,%lo(__mx_builtin_gc_sz)
	li	s2,-1
	add	t2,a5,t2
	j	.L39
.L29:
	addi	t5,t5,4
	blt	t3,t6,.L28
.L39:
	lw	a6,0(t5)
	addi	t6,t6,1
	slli	a6,a6,2
	add	a5,s0,a6
	lw	s3,0(a5)
	ble	s3,zero,.L29
	add	s5,t0,a6
	lw	a4,0(s5)
	bge	a4,zero,.L29
	neg	a5,a4
	andi	a5,a5,3
	bne	a5,zero,.L29
	sub	s6,s3,a4
	mv	s4,s3
	bgeu	s3,s6,.L30
.L38:
	lw	a0,0(s4)
	blt	t1,zero,.L31
	mv	a3,t1
	li	a7,-1
	li	a4,0
.L36:
	add	a5,a3,a4
	srai	a5,a5,1
	slli	a2,a5,2
	add	a1,s0,a2
	lw	a1,0(a1)
	blt	a0,a1,.L32
.L58:
	addi	a4,a5,1
	bgt	a4,a3,.L34
	mv	a7,a5
	add	a5,a3,a4
	srai	a5,a5,1
	slli	a2,a5,2
	add	a1,s0,a2
	lw	a1,0(a1)
	bge	a0,a1,.L58
.L32:
	addi	a3,a5,-1
	bge	a3,a4,.L36
	beq	a7,s2,.L31
	mv	a5,a7
	slli	a2,a7,2
.L34:
	add	a4,s0,a2
	lw	a4,0(a4)
	beq	a0,a4,.L37
	addi	a4,a4,4
	bne	a0,a4,.L31
.L37:
	add	a2,t0,a2
	lw	a4,0(a2)
	ble	a4,zero,.L31
	neg	a4,a4
	sw	a4,0(a2)
	lw	a4,0(s5)
	addi	t3,t3,1
	slli	a3,t3,2
	add	a3,t2,a3
	sw	a5,84(a3)
	sub	s6,s3,a4
.L31:
	addi	s4,s4,4
	bltu	s4,s6,.L38
.L30:
	add	a6,s0,a6
	neg	a5,s3
	sw	a5,0(a6)
	addi	t5,t5,4
	bge	t3,t6,.L39
.L28:
	lui	s6,%hi(__mx_builtin_gc_malloced)
	sw	zero,%lo(__mx_builtin_gc_malloced)(s6)
	li	s5,0
	ble	t4,zero,.L19
	lui	s3,%hi(__mx_builtin_gc_sz)
	lui	s2,%hi(__mx_builtin_gc_a)
	addi	s7,s3,%lo(__mx_builtin_gc_sz)
	addi	s0,s2,%lo(__mx_builtin_gc_a)
	addi	s3,s3,%lo(__mx_builtin_gc_sz)
	addi	s2,s2,%lo(__mx_builtin_gc_a)
	li	s4,0
	li	s5,0
.L43:
	lw	a4,0(s3)
	slli	a5,s5,2
	add	a6,s0,a5
	lw	a0,0(s2)
	add	a5,s7,a5
	neg	a1,a4
	bge	a4,zero,.L40
	lw	a3,%lo(__mx_builtin_gc_malloced)(s6)
	srai	a2,a0,31
	xor	a0,a2,a0
	lw	a7,%lo(__mx_builtin_gc_en)(s1)
	sub	a0,a0,a2
	sub	a4,a3,a4
	sw	a0,0(a6)
	sw	a1,0(a5)
	sw	a4,%lo(__mx_builtin_gc_malloced)(s6)
	addi	s4,s4,1
	addi	s5,s5,1
	addi	s3,s3,4
	addi	s2,s2,4
	bgt	a7,s4,.L43
.L19:
	sw	s5,%lo(__mx_builtin_gc_en)(s1)
	addi	sp,sp,2032
	lw	ra,2028(sp)
	lw	s0,2024(sp)
	lw	s1,2020(sp)
	lw	s2,2016(sp)
	lw	s3,2012(sp)
	lw	s4,2008(sp)
	lw	s5,2004(sp)
	lw	s6,2000(sp)
	lw	s7,1996(sp)
	addi	sp,sp,2032
	jr	ra
.L40:
	call	free
	lw	a5,%lo(__mx_builtin_gc_en)(s1)
	addi	s4,s4,1
	addi	s3,s3,4
	addi	s2,s2,4
	bgt	a5,s4,.L43
	j	.L19
	.size	__mx_builtin_gc, .-__mx_builtin_gc
	.align	2
	.globl	__mx_builtin_malloc
	.type	__mx_builtin_malloc, @function
__mx_builtin_malloc:
	addi	sp,sp,-32
	sw	s2,16(sp)
	lui	s2,%hi(__mx_builtin_gc_malloced)
	lw	a5,%lo(__mx_builtin_gc_malloced)(s2)
	sw	s0,24(sp)
	sw	s3,12(sp)
	sw	ra,28(sp)
	sw	s1,20(sp)
	add	a5,a0,a5
	li	a4,102400
	mv	s0,a0
	lui	s3,%hi(__mx_builtin_gc_en)
	bgt	a5,a4,.L60
	lw	s1,%lo(__mx_builtin_gc_en)(s3)
	li	a4,999
	bgt	s1,a4,.L60
.L61:
	mv	a0,s0
	sw	a5,%lo(__mx_builtin_gc_malloced)(s2)
	call	malloc
	lui	a4,%hi(__mx_builtin_gc_a)
	lui	a5,%hi(__mx_builtin_gc_sz)
	slli	a3,s1,2
	addi	a4,a4,%lo(__mx_builtin_gc_a)
	addi	a5,a5,%lo(__mx_builtin_gc_sz)
	add	a4,a4,a3
	add	a5,a5,a3
	addi	s1,s1,1
	sw	a0,0(a4)
	sw	s0,0(a5)
	sw	s1,%lo(__mx_builtin_gc_en)(s3)
.L59:
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s1,20(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	addi	sp,sp,32
	jr	ra
.L60:
	call	__mx_builtin_gc_before
	lw	s1,%lo(__mx_builtin_gc_en)(s3)
	li	a5,999
	li	a0,0
	bgt	s1,a5,.L59
	lw	a5,%lo(__mx_builtin_gc_malloced)(s2)
	add	a5,s0,a5
	j	.L61
	.size	__mx_builtin_malloc, .-__mx_builtin_malloc
	.section	.rodata.str1.4,"aMS",@progbits,1
	.align	2
.LC0:
	.string	"%s"
	.text
	.align	2
	.globl	__mx_builtin_print
	.type	__mx_builtin_print, @function
__mx_builtin_print:
	mv	a1,a0
	lui	a0,%hi(.LC0)
	addi	a0,a0,%lo(.LC0)
	tail	printf
	.size	__mx_builtin_print, .-__mx_builtin_print
	.align	2
	.globl	__mx_builtin_println
	.type	__mx_builtin_println, @function
__mx_builtin_println:
	tail	puts
	.size	__mx_builtin_println, .-__mx_builtin_println
	.section	.rodata.str1.4
	.align	2
.LC1:
	.string	"%d"
	.text
	.align	2
	.globl	__mx_builtin_printInt
	.type	__mx_builtin_printInt, @function
__mx_builtin_printInt:
	mv	a1,a0
	lui	a0,%hi(.LC1)
	addi	a0,a0,%lo(.LC1)
	tail	printf
	.size	__mx_builtin_printInt, .-__mx_builtin_printInt
	.section	.rodata.str1.4
	.align	2
.LC2:
	.string	"%d\n"
	.text
	.align	2
	.globl	__mx_builtin_printlnInt
	.type	__mx_builtin_printlnInt, @function
__mx_builtin_printlnInt:
	mv	a1,a0
	lui	a0,%hi(.LC2)
	addi	a0,a0,%lo(.LC2)
	tail	printf
	.size	__mx_builtin_printlnInt, .-__mx_builtin_printlnInt
	.align	2
	.globl	__mx_builtin_getString
	.type	__mx_builtin_getString, @function
__mx_builtin_getString:
	addi	sp,sp,-32
	sw	s3,12(sp)
	lui	s3,%hi(__mx_builtin_gc_malloced)
	lw	a5,%lo(__mx_builtin_gc_malloced)(s3)
	li	a4,98304
	sw	s4,8(sp)
	sw	ra,28(sp)
	sw	s0,24(sp)
	sw	s1,20(sp)
	sw	s2,16(sp)
	addi	a4,a4,1763
	lui	s4,%hi(__mx_builtin_gc_en)
	bgt	a5,a4,.L72
	lw	s0,%lo(__mx_builtin_gc_en)(s4)
	li	a4,999
	bgt	s0,a4,.L72
.L73:
	li	s2,4096
	addi	s2,s2,-1763
	add	a5,a5,s2
	mv	a0,s2
	sw	a5,%lo(__mx_builtin_gc_malloced)(s3)
	call	malloc
	lui	a4,%hi(__mx_builtin_gc_a)
	lui	a5,%hi(__mx_builtin_gc_sz)
	slli	a3,s0,2
	addi	a4,a4,%lo(__mx_builtin_gc_a)
	addi	a5,a5,%lo(__mx_builtin_gc_sz)
	add	a4,a4,a3
	add	a5,a5,a3
	addi	s0,s0,1
	mv	s1,a0
	sw	a0,0(a4)
	sw	s2,0(a5)
	sw	s0,%lo(__mx_builtin_gc_en)(s4)
.L74:
	lui	a0,%hi(.LC0)
	mv	a1,s1
	addi	a0,a0,%lo(.LC0)
	call	scanf
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	lw	s4,8(sp)
	mv	a0,s1
	lw	s1,20(sp)
	addi	sp,sp,32
	jr	ra
.L72:
	call	__mx_builtin_gc_before
	lw	s0,%lo(__mx_builtin_gc_en)(s4)
	li	a5,999
	li	s1,0
	bgt	s0,a5,.L74
	lw	a5,%lo(__mx_builtin_gc_malloced)(s3)
	j	.L73
	.size	__mx_builtin_getString, .-__mx_builtin_getString
	.align	2
	.globl	__mx_builtin_getInt
	.type	__mx_builtin_getInt, @function
__mx_builtin_getInt:
	addi	sp,sp,-32
	lui	a0,%hi(.LC1)
	addi	a1,sp,12
	addi	a0,a0,%lo(.LC1)
	sw	ra,28(sp)
	call	scanf
	lw	ra,28(sp)
	lw	a0,12(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_builtin_getInt, .-__mx_builtin_getInt
	.align	2
	.globl	__mx_builtin_toString
	.type	__mx_builtin_toString, @function
__mx_builtin_toString:
	addi	sp,sp,-32
	sw	s3,12(sp)
	lui	s3,%hi(__mx_builtin_gc_malloced)
	lw	a5,%lo(__mx_builtin_gc_malloced)(s3)
	li	a4,102400
	sw	s2,16(sp)
	sw	s4,8(sp)
	sw	ra,28(sp)
	sw	s0,24(sp)
	sw	s1,20(sp)
	addi	a4,a4,-23
	mv	s2,a0
	lui	s4,%hi(__mx_builtin_gc_en)
	bgt	a5,a4,.L82
	lw	s0,%lo(__mx_builtin_gc_en)(s4)
	li	a4,999
	bgt	s0,a4,.L82
.L83:
	addi	a5,a5,23
	li	a0,23
	sw	a5,%lo(__mx_builtin_gc_malloced)(s3)
	call	malloc
	lui	a4,%hi(__mx_builtin_gc_a)
	slli	a3,s0,2
	lui	a5,%hi(__mx_builtin_gc_sz)
	addi	a4,a4,%lo(__mx_builtin_gc_a)
	add	a4,a4,a3
	addi	a5,a5,%lo(__mx_builtin_gc_sz)
	add	a5,a5,a3
	addi	s0,s0,1
	sw	a0,0(a4)
	li	a4,23
	mv	s1,a0
	sw	a4,0(a5)
	sw	s0,%lo(__mx_builtin_gc_en)(s4)
.L84:
	lui	a1,%hi(.LC1)
	mv	a2,s2
	mv	a0,s1
	addi	a1,a1,%lo(.LC1)
	call	sprintf
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	lw	s4,8(sp)
	mv	a0,s1
	lw	s1,20(sp)
	addi	sp,sp,32
	jr	ra
.L82:
	call	__mx_builtin_gc_before
	lw	s0,%lo(__mx_builtin_gc_en)(s4)
	li	a5,999
	li	s1,0
	bgt	s0,a5,.L84
	lw	a5,%lo(__mx_builtin_gc_malloced)(s3)
	j	.L83
	.size	__mx_builtin_toString, .-__mx_builtin_toString
	.align	2
	.globl	__mx_builtin_str_length
	.type	__mx_builtin_str_length, @function
__mx_builtin_str_length:
	tail	strlen
	.size	__mx_builtin_str_length, .-__mx_builtin_str_length
	.align	2
	.globl	__mx_builtin_str_substring
	.type	__mx_builtin_str_substring, @function
__mx_builtin_str_substring:
	addi	sp,sp,-32
	sw	s5,4(sp)
	lui	s5,%hi(__mx_builtin_gc_malloced)
	lw	a5,%lo(__mx_builtin_gc_malloced)(s5)
	sw	s0,24(sp)
	sub	s0,a2,a1
	sw	s4,8(sp)
	addi	s4,s0,1
	sw	s2,16(sp)
	sw	s3,12(sp)
	sw	s6,0(sp)
	sw	ra,28(sp)
	sw	s1,20(sp)
	add	a5,s4,a5
	li	a4,102400
	mv	s2,a1
	mv	s3,a0
	lui	s6,%hi(__mx_builtin_gc_en)
	bgt	a5,a4,.L91
	lw	s1,%lo(__mx_builtin_gc_en)(s6)
	li	a4,999
	bgt	s1,a4,.L91
.L92:
	mv	a0,s4
	sw	a5,%lo(__mx_builtin_gc_malloced)(s5)
	call	malloc
	lui	a3,%hi(__mx_builtin_gc_a)
	lui	a4,%hi(__mx_builtin_gc_sz)
	slli	a2,s1,2
	addi	a3,a3,%lo(__mx_builtin_gc_a)
	addi	a4,a4,%lo(__mx_builtin_gc_sz)
	add	a3,a3,a2
	add	a4,a4,a2
	addi	s1,s1,1
	mv	a5,a0
	sw	a0,0(a3)
	sw	s4,0(a4)
	sw	s1,%lo(__mx_builtin_gc_en)(s6)
.L93:
	add	a1,s3,s2
	mv	a2,s0
	mv	a0,a5
	call	memcpy
	add	s0,a0,s0
	sb	zero,0(s0)
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s1,20(sp)
	lw	s2,16(sp)
	lw	s3,12(sp)
	lw	s4,8(sp)
	lw	s5,4(sp)
	lw	s6,0(sp)
	addi	sp,sp,32
	jr	ra
.L91:
	call	__mx_builtin_gc_before
	lw	s1,%lo(__mx_builtin_gc_en)(s6)
	li	a4,999
	li	a5,0
	bgt	s1,a4,.L93
	lw	a5,%lo(__mx_builtin_gc_malloced)(s5)
	add	a5,s4,a5
	j	.L92
	.size	__mx_builtin_str_substring, .-__mx_builtin_str_substring
	.align	2
	.globl	__mx_builtin_str_parseInt
	.type	__mx_builtin_str_parseInt, @function
__mx_builtin_str_parseInt:
	addi	sp,sp,-32
	lui	a1,%hi(.LC1)
	addi	a2,sp,12
	addi	a1,a1,%lo(.LC1)
	sw	ra,28(sp)
	call	sscanf
	lw	ra,28(sp)
	lw	a0,12(sp)
	addi	sp,sp,32
	jr	ra
	.size	__mx_builtin_str_parseInt, .-__mx_builtin_str_parseInt
	.align	2
	.globl	__mx_builtin_str_ord
	.type	__mx_builtin_str_ord, @function
__mx_builtin_str_ord:
	add	a0,a0,a1
	lbu	a0,0(a0)
	ret
	.size	__mx_builtin_str_ord, .-__mx_builtin_str_ord
	.align	2
	.globl	__mx_builtin_str_add
	.type	__mx_builtin_str_add, @function
__mx_builtin_str_add:
	addi	sp,sp,-16
	sw	ra,12(sp)
	sw	s0,8(sp)
	sw	s1,4(sp)
	sw	s2,0(sp)
	mv	s2,a0
	mv	s1,a1
	li	a0,4096
	addi	a0,a0,-1763
	call	__mx_builtin_malloc
	mv	s0,a0
	mv	a1,s2
	call	strcpy
	mv	a1,s1
	mv	a0,s0
	call	strcat
	mv	a0,s0
	lw	ra,12(sp)
	lw	s0,8(sp)
	lw	s1,4(sp)
	lw	s2,0(sp)
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_add, .-__mx_builtin_str_add
	.align	2
	.globl	__mx_builtin_str_lt
	.type	__mx_builtin_str_lt, @function
__mx_builtin_str_lt:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	srli	a0,a0,31
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_lt, .-__mx_builtin_str_lt
	.align	2
	.globl	__mx_builtin_str_gt
	.type	__mx_builtin_str_gt, @function
__mx_builtin_str_gt:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	sgt	a0,a0,zero
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_gt, .-__mx_builtin_str_gt
	.align	2
	.globl	__mx_builtin_str_le
	.type	__mx_builtin_str_le, @function
__mx_builtin_str_le:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	slti	a0,a0,1
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_le, .-__mx_builtin_str_le
	.align	2
	.globl	__mx_builtin_str_ge
	.type	__mx_builtin_str_ge, @function
__mx_builtin_str_ge:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	not	a0,a0
	srli	a0,a0,31
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_ge, .-__mx_builtin_str_ge
	.align	2
	.globl	__mx_builtin_str_eq
	.type	__mx_builtin_str_eq, @function
__mx_builtin_str_eq:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	seqz	a0,a0
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_eq, .-__mx_builtin_str_eq
	.align	2
	.globl	__mx_builtin_str_ne
	.type	__mx_builtin_str_ne, @function
__mx_builtin_str_ne:
	addi	sp,sp,-16
	sw	ra,12(sp)
	call	strcmp
	lw	ra,12(sp)
	snez	a0,a0
	addi	sp,sp,16
	jr	ra
	.size	__mx_builtin_str_ne, .-__mx_builtin_str_ne
	.globl	__mx_builtin_gc_malloced
	.globl	__mx_builtin_gc_en
	.globl	__mx_builtin_gc_sz
	.globl	__mx_builtin_gc_a
	.globl	__mx_builtin_gc_sps
	.globl	__mx_builtin_gc_pool_size
	.bss
	.align	2
	.type	__mx_builtin_gc_sz, @object
	.size	__mx_builtin_gc_sz, 4012
__mx_builtin_gc_sz:
	.zero	4012
	.type	__mx_builtin_gc_a, @object
	.size	__mx_builtin_gc_a, 4012
__mx_builtin_gc_a:
	.zero	4012
	.section	.sbss,"aw",@nobits
	.align	2
	.type	__mx_builtin_gc_malloced, @object
	.size	__mx_builtin_gc_malloced, 4
__mx_builtin_gc_malloced:
	.zero	4
	.type	__mx_builtin_gc_en, @object
	.size	__mx_builtin_gc_en, 4
__mx_builtin_gc_en:
	.zero	4
	.type	__mx_builtin_gc_sps, @object
	.size	__mx_builtin_gc_sps, 4
__mx_builtin_gc_sps:
	.zero	4
	.section	.srodata,"a"
	.align	2
	.type	__mx_builtin_gc_pool_size, @object
	.size	__mx_builtin_gc_pool_size, 4
__mx_builtin_gc_pool_size:
	.word	102400
	.ident	"GCC: (GNU) 10.2.0"