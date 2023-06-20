package tripleo.elijah.stages.write_stage.pipeline_impl;

public class WPIS_MakeOutputDirectory implements WP_Indiviual_Step {

	@Override
	public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
		// 2. make output directory
		// TODO check first
		System.out.println("77-09 " + st.file_prefix);
		boolean made = st.file_prefix.mkdirs();
	}
}
