package index.mondial;

import util.Params;

public class buildGraph {

	public static void main(String[] args) {
        Params.setExpDB("pittsburgh");
//		MondialGraphBuilder gb = new MondialGraphBuilder();
		MondialGraphBuilderRest gb = new MondialGraphBuilderRest();
//		gb.createTest();


		System.out.println("processing Continent...");
		gb.createContinent();

		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Country...");
		gb.createCountry();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Religion...");
		gb.createReligion();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Politics...");
		gb.createPolitics();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing BorderRelation...");		
//		gb.createBorderRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());
		
		System.out.println("processing Province...");
		gb.createProvince();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing City...");
		gb.createCity();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Organization...");
		gb.createOrganization();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing MemberRelation...");		
		gb.createMemberRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Sea...");
		gb.createSea();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Lake...");
		gb.createLake();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing River...");
		gb.createRiver();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());
		
		System.out.println("processing Island...");
		gb.createIsland();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Mountain...");
		gb.createMountain();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Desert...");
		gb.createDesert();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing IsIslandInRelation...");
		gb.createIsIslandInRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing LocatedRelation...");
		gb.createLocatedRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("successfully finished.");
		
		gb.computeNodeNodeRel();		
		// then, we must load NNRData file to NNR table in MySQL manually
		// before executing buildIndex.class
		
		gb.close();
	}
}
