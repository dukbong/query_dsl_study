package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    void testEntity() {
        // given
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

        em.flush();
        em.clear();

        // when
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        // then
        assertThat(members).hasSize(4)
                .extracting("id", "username", "age", "team.id", "team.name")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(member1.getId(), member1.getUsername(), member1.getAge(), teamA.getId(), teamA.getName()),
                        Tuple.tuple(member2.getId(), member2.getUsername(), member2.getAge(), teamA.getId(), teamA.getName()),
                        Tuple.tuple(member3.getId(), member3.getUsername(), member3.getAge(), teamB.getId(), teamB.getName()),
                        Tuple.tuple(member4.getId(), member4.getUsername(), member4.getAge(), teamB.getId(), teamB.getName())
                );
    }
}