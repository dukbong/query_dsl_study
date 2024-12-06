package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    @BeforeEach
    void beforeEach() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        // 요구 사항 1. member1을 찾아라
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        // 요구 사항 1 검증
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQueryDSL() {
        // 요구 사항 1. member1을 찾아라
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember m = QMember.member;

        Member findMember = query
                .select(m)
                .from(m)
                .where( m.username.eq("member1") )
                .fetchOne();

        assert findMember != null;
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        // given
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember m = QMember.member;

        Member findMember = query
                .select(m)
                .from(m)
                .where(
                        m.username.eq("member1")
                                .and(
                                        m.age.eq(10)
                                )
                )
                .fetchOne();

        assert findMember != null;
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember m = QMember.member;
        // given
        List<Member> fetch = query
                .select(m)
                .from(m)
                .fetch();

        try {
            Member findMember = query
                    .select(m)
                    .from(m)
                    .fetchOne();
        } catch(NonUniqueResultException ex) {
            ex.printStackTrace();
        }


        Member fetchFirst = query
                .select(m)
                .from(m)
                .fetchFirst();

        QueryResults<Member> results = query
                .select(m)
                .from(m)
                .fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();

        long total = query
                .select(m)
                .from(m)
                .fetchCount();
    }

    @Test
    @DisplayName("나이 내림차순, 이름 올림차순, 단 이름이 없으면 마지막 출력")
    void sort() {
        // given
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember m = QMember.member;
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        // when
        List<Member> result = query.select(m)
                .from(m)
                .where(m.age.eq(100))
                .orderBy(m.age.desc(), m.username.asc().nullsLast())
                .fetch();
        // then
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isEqualTo(null);
        assertThat(member5.getAge()).isEqualTo(100);
        assertThat(member6.getAge()).isEqualTo(100);
        assertThat(memberNull.getAge()).isEqualTo(100);

        assertThat(result)
                .hasSize(3)
                .extracting("username", "age")
                .containsExactly(
                        tuple("member5", 100),
                        tuple("member6", 100),
                        tuple(null, 100)
                );

    }
}
