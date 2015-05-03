package com.slgerkamp.introductory.rxjava.chapter01;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * <p>参考：7つのサンプルプログラムで学ぶRxJavaの挙動
 * <p>http://techlife.cookpad.com/entry/2015/04/13/170000
 */
public class HowToUseRxJavaTest {

	@Test
	public void RxJavaの基本的な動きを確認する(){
		final StringBuffer sb = new StringBuffer();
		sb.append("start:");
		Observable.just(10)
		.map(
				// map
				t -> { sb.append("map"); return t;})
		.subscribe(
				// onNext
				t -> sb.append("onNext"), 
				// onError
				error -> sb.append("onError"), 
				// onComplete
				() -> sb.append("onComplete"));
		sb.append("end");
		
		assertThat(sb.toString(), is("start:maponNextonCompleteend"));
	}

	@Test
	public void RxJavaの基本的な動きを確認する_スリープ(){
		final StringBuffer sb = new StringBuffer();
		sb.append("start:");
		Observable.just(10)
		.map(
				// map
				t -> { 
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
					sb.append("map"); return t;})
		.subscribe(
				// onNext
				t -> sb.append("onNext"), 
				// onError
				error -> sb.append("onError"), 
				// onComplete
				() -> {
					sb.append("onComplete");
				});
		sb.append("end");
		
		assertThat(sb.toString(), is("start:maponNextonCompleteend"));
	}


	@Test
	public void RxJavaの基本的な動きを確認する_subscribeOnを使用() throws InterruptedException{
		final CountDownLatch latch = new CountDownLatch(1);
		final StringBuffer sb = new StringBuffer();
		final List<String> list = new ArrayList<>();
		
		// メインスレッドで実行
		sb.append("start:");
		
		Observable.just(10)
		// 別スレッドを立ち上げる
		.subscribeOn(Schedulers.newThread())
		.map(
				// map
				t -> {  
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
					sb.append("map");
					list.add("1:" + Thread.currentThread().getName());
					return t;
		})
		.subscribe(
				// onNext
				t -> {
					sb.append("onNext");
					list.add("2:" + Thread.currentThread().getName());
				}, 
				// onError
				error -> {
					list.add("4:" + Thread.currentThread().getName());
					sb.append("onError"); 
				},
				// onComplete
				() -> {
					list.add("3:" + Thread.currentThread().getName());
					sb.append("onComplete");
				});

		// メインスレッドで実行
		sb.append("end");
		
		latch.await(1, TimeUnit.SECONDS);
		assertThat(sb.toString(), is("start:endmaponNextonComplete"));
		IntStream.range(0, list.size()).forEach(
				i -> assertThat(list.get(i), is(startsWith(i+1 + ":RxNewThreadScheduler")))
		);
	}

	
}
