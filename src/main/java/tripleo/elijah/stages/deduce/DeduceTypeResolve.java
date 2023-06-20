/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.lang.impl.AbstractCodeGen;
import tripleo.elijah.lang.impl.AliasStatementImpl;
import tripleo.elijah.lang.impl.MatchConditionalImpl;
import tripleo.elijah.lang.impl.VariableStatementImpl;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.SGTA_SetResolvedClass;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;

/**
 * Created 11/18/21 12:02 PM
 */
public class DeduceTypeResolve {
	BaseTableEntry backlink;
	private final BaseTableEntry                              bte;
	private final DeferredObject<GenType, ResolveError, Void> typeResolution = new DeferredObject<GenType, ResolveError, Void>();

	public DeduceTypeResolve(BaseTableEntry aBte) {
		bte = aBte;
		if (bte instanceof IdentTableEntry) {
			((IdentTableEntry) bte).backlinkSet().then(new DoneCallback<InstructionArgument>() {
				@Override
				public void onDone(final InstructionArgument backlink0) {
					if (backlink0 instanceof IdentIA) {
						backlink = ((IdentIA) backlink0).getEntry();
						setBacklinkCallback();
					} else if (backlink0 instanceof IntegerIA) {
						backlink = ((IntegerIA) backlink0).getEntry();
						setBacklinkCallback();
					} else if (backlink0 instanceof ProcIA) {
						backlink = ((ProcIA) backlink0).getEntry();
						setBacklinkCallback();
					} else
						backlink = null;
				}
			});
		} else if (bte instanceof VariableTableEntry) {
			backlink = null;
		} else if (bte instanceof ProcTableEntry) {
			backlink = null;
		} else
			throw new IllegalStateException();

		if (backlink != null) {
		} else {
			bte.addStatusListener(new _StatusListener__BTE_86());
		}
	}

	protected void setBacklinkCallback() {
		backlink.addStatusListener(new _StatusListener__BTE_203());
	}

	public Promise<GenType, ResolveError, Void> typeResolution() {
		return typeResolution.promise();
	}

	private class _StatusListener__BTE_86 implements BaseTableEntry.StatusListener {
		GenType genType = new GenType();

		@Override
		public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
			if (newStatus != BaseTableEntry.Status.KNOWN) return;

			eh.getElement().visitGen(new AbstractCodeGen() {
				@Override
				public void addClass(final ClassStatement klass) {
					genType.resolved = klass.getOS_Type();

					if (eh instanceof DeduceElement3_IdentTableEntry.DE3_ITE_Holder) {
						DeduceElement3_IdentTableEntry.DE3_ITE_Holder de3_ite_holder = (DeduceElement3_IdentTableEntry.DE3_ITE_Holder) eh;
						de3_ite_holder.genTypeAction(new SGTA_SetResolvedClass(klass));
					}
				}

				@Override
				public void defaultAction(final OS_Element anElement) {
					logProgress(158, "158 " + anElement);
					throw new IllegalStateException();
				}

				@Override
				public void visitAliasStatement(final AliasStatementImpl aAliasStatement) {
					logProgress(127, String.format("** AliasStatementImpl %s points to %s", aAliasStatement.name(), aAliasStatement.getExpression()));
				}

				@Override
				public void visitConstructorDef(final ConstructorDef aConstructorDef) {
					int y = 2;
				}

				@Override
				public void visitDefFunction(final DefFunctionDef aDefFunctionDef) {
					logProgress(138, String.format("** DefFunctionDef %s is %s", aDefFunctionDef.name(), ((StatementWrapper) aDefFunctionDef.getItems().iterator().next()).getExpr()));
				}

				@Override
				public void visitFormalArgListItem(final FormalArgListItem aFormalArgListItem) {
					final OS_Type attached;
					if (bte instanceof VariableTableEntry)
						attached = ((VariableTableEntry) bte).type.getAttached();
					else if (bte instanceof IdentTableEntry) {
						final IdentTableEntry identTableEntry = (IdentTableEntry) DeduceTypeResolve.this.bte;
						if (identTableEntry.type == null)
							return;
						attached = identTableEntry.type.getAttached();
					} else
						throw new IllegalStateException("invalid entry (bte) " + bte);

					if (attached != null)
						logProgress(155,

									String.format("** FormalArgListItem %s attached is not null. Type is %s. Points to %s",
												  aFormalArgListItem.name(), aFormalArgListItem.typeName(), attached));
					else
						logProgress(159,
									String.format("** FormalArgListItem %s attached is null. Type is %s.",
												  aFormalArgListItem.name(), aFormalArgListItem.typeName()));
				}

				@Override
				public void visitFunctionDef(final FunctionDef aFunctionDef) {
					genType.resolved = aFunctionDef.getOS_Type();
				}

				@Override
				public void visitIdentExpression(final IdentExpression aIdentExpression) {
					new DTR_IdentExpression(DeduceTypeResolve.this, aIdentExpression, bte).run(eh, genType);
				}

				@Override
				public void visitMC1(final MatchConditional.MC1 aMC1) {
					if (aMC1 instanceof final MatchConditionalImpl.MatchArm_TypeMatch typeMatch) {
						int yy = 2;
					}
				}

				@Override
				public void visitPropertyStatement(final PropertyStatement aPropertyStatement) {
					genType.typeName = new OS_UserType(aPropertyStatement.getTypeName());
					// TODO resolve??
				}

				@Override
				public void visitVariableStatement(final VariableStatementImpl variableStatement) {
					new DTR_VariableStatement(DeduceTypeResolve.this, variableStatement).run(eh, genType);
				}

			});

			if (!typeResolution.isPending()) {
				int y = 2;
			} else {
				if (!genType.isNull())
					typeResolution.resolve(genType);
			}
		}

		public void logProgress(int ignoredCode, String message) {
			tripleo.elijah.util.Stupidity.println_err_2(message);
		}
	}

	private class _StatusListener__BTE_203 implements BaseTableEntry.StatusListener {
		@Override
		public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
			if (newStatus != BaseTableEntry.Status.KNOWN) return;

			if (backlink instanceof final IdentTableEntry identTableEntry) {
				identTableEntry.typeResolvePromise().done(result -> _203_backlink_isIDTE(result, identTableEntry));
			} else if (backlink instanceof final VariableTableEntry variableTableEntry) {
				variableTableEntry.typeResolvePromise().done(result -> _203_backlink_is_VTE(result, eh, variableTableEntry));
			} else if (backlink instanceof ProcTableEntry) {
				final ProcTableEntry procTableEntry = (ProcTableEntry) backlink;
				procTableEntry.typeResolvePromise().done(result -> _203_backlink_is_PTE(result, procTableEntry, eh));
			}
		}

		private void _203_backlink_isIDTE(final GenType result, final IdentTableEntry identTableEntry) {
			if (identTableEntry.type == null) {
				identTableEntry.type = new TypeTableEntry(999, TypeTableEntry.Type.TRANSIENT, result.typeName, identTableEntry.getIdent(), null);
			}

			identTableEntry.type.setAttached(result);
		}

		private void _203_backlink_is_VTE(final GenType result, final IElementHolder eh, final VariableTableEntry variableTableEntry) {
			if (eh instanceof final Resolve_Ident_IA.GenericElementHolderWithDC eh1) {
				final DeduceTypes2.DeduceClient3 dc = eh1.getDC();
				dc.genCIForGenType2(result);
			}
			// maybe set something in ci to INHERITED, but thats what DeduceProcCall is for
			if (eh.getElement() instanceof FunctionDef) {
				if (result.node instanceof final EvaClass evaClass) {
					evaClass.functionMapDeferred((FunctionDef) eh.getElement(), new FunctionMapDeferred() {
						@Override
						public void onNotify(final EvaFunction aGeneratedFunction) {
							result.node = aGeneratedFunction;
						}
					});
				}
			}
			variableTableEntry.type.setAttached(result);
		}

		private void _203_backlink_is_PTE(final GenType result, final ProcTableEntry procTableEntry, final IElementHolder eh) {
			// README
			//   1. Resolve type of pte (above) to a class
			//   2. Convert bte to ite
			//   3. Get DeduceElement3
			//   4. Pass it off

			// README classStatement [T310-231]
			final ClassStatement classStatement = result.resolved.getClassOf();

			if (bte instanceof IdentTableEntry) {
			} else
				throw new AssertionError();

			final IdentTableEntry                identTableEntry_bte = (IdentTableEntry) bte;
			final DeduceElement3_IdentTableEntry de3_ite             = identTableEntry_bte.getDeduceElement3();

			// Just testing
			final DeduceElement3_IdentTableEntry de3_ite2 = identTableEntry_bte.__dt2._zero.getIdent(identTableEntry_bte, identTableEntry_bte.__gf, identTableEntry_bte.__dt2);
			assert de3_ite2.principal == de3_ite.principal;

			// Also testing, but not essential
			//assert identTableEntry_bte.getCallablePTE() != null && procTableEntry == identTableEntry_bte.getCallablePTE();

			de3_ite.backlinkPte(classStatement, procTableEntry, eh);
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
